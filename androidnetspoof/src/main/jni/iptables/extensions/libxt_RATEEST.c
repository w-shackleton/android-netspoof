#include <stdbool.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <stddef.h>
#include <getopt.h>
#include <math.h>

#include <xtables.h>
#include <linux/netfilter/x_tables.h>
#include <linux/netfilter/xt_RATEEST.h>

/* hack to pass raw values to final_check */
static struct xt_rateest_target_info *RATEEST_info;
static unsigned int interval;
static unsigned int ewma_log;

static void
RATEEST_help(void)
{
	printf(
"RATEEST target options:\n"
"  --rateest-name name		Rate estimator name\n"
"  --rateest-interval sec	Rate measurement interval in seconds\n"
"  --rateest-ewmalog value	Rate measurement averaging time constant\n");
}

enum RATEEST_options {
	RATEEST_OPT_NAME,
	RATEEST_OPT_INTERVAL,
	RATEEST_OPT_EWMALOG,
};

static const struct option RATEEST_opts[] = {
	{.name = "rateest-name",     .has_arg = true, .val = RATEEST_OPT_NAME},
	{.name = "rateest-interval", .has_arg = true, .val = RATEEST_OPT_INTERVAL},
	{.name = "rateest-ewmalog",  .has_arg = true, .val = RATEEST_OPT_EWMALOG},
	XT_GETOPT_TABLEEND,
};

/* Copied from iproute */
#define TIME_UNITS_PER_SEC	1000000

static int
RATEEST_get_time(unsigned int *time, const char *str)
{
	double t;
	char *p;

	t = strtod(str, &p);
	if (p == str)
		return -1;

	if (*p) {
		if (strcasecmp(p, "s") == 0 || strcasecmp(p, "sec")==0 ||
		    strcasecmp(p, "secs")==0)
			t *= TIME_UNITS_PER_SEC;
		else if (strcasecmp(p, "ms") == 0 || strcasecmp(p, "msec")==0 ||
			 strcasecmp(p, "msecs") == 0)
			t *= TIME_UNITS_PER_SEC/1000;
		else if (strcasecmp(p, "us") == 0 || strcasecmp(p, "usec")==0 ||
			 strcasecmp(p, "usecs") == 0)
			t *= TIME_UNITS_PER_SEC/1000000;
		else
			return -1;
	}

	*time = t;
	return 0;
}

static void
RATEEST_print_time(unsigned int time)
{
	double tmp = time;

	if (tmp >= TIME_UNITS_PER_SEC)
		printf("%.1fs ", tmp/TIME_UNITS_PER_SEC);
	else if (tmp >= TIME_UNITS_PER_SEC/1000)
		printf("%.1fms ", tmp/(TIME_UNITS_PER_SEC/1000));
	else
		printf("%uus ", time);
}

static void
RATEEST_init(struct xt_entry_target *target)
{
	interval = 0;
	ewma_log = 0;
}

static int
RATEEST_parse(int c, char **argv, int invert, unsigned int *flags,
	      const void *entry, struct xt_entry_target **target)
{
	struct xt_rateest_target_info *info = (void *)(*target)->data;

	RATEEST_info = info;

	switch (c) {
	case RATEEST_OPT_NAME:
		if (*flags & (1 << c))
			xtables_error(PARAMETER_PROBLEM,
				   "RATEEST: can't specify --rateest-name twice");
		*flags |= 1 << c;

		strncpy(info->name, optarg, sizeof(info->name) - 1);
		break;

	case RATEEST_OPT_INTERVAL:
		if (*flags & (1 << c))
			xtables_error(PARAMETER_PROBLEM,
				   "RATEEST: can't specify --rateest-interval twice");
		*flags |= 1 << c;

		if (RATEEST_get_time(&interval, optarg) < 0)
			xtables_error(PARAMETER_PROBLEM,
				   "RATEEST: bad interval value `%s'", optarg);

		break;

	case RATEEST_OPT_EWMALOG:
		if (*flags & (1 << c))
			xtables_error(PARAMETER_PROBLEM,
				   "RATEEST: can't specify --rateest-ewmalog twice");
		*flags |= 1 << c;

		if (RATEEST_get_time(&ewma_log, optarg) < 0)
			xtables_error(PARAMETER_PROBLEM,
				   "RATEEST: bad ewmalog value `%s'", optarg);

		break;

	default:
		return 0;
	}

	return 1;
}

static void
RATEEST_final_check(unsigned int flags)
{
	struct xt_rateest_target_info *info = RATEEST_info;

	if (!(flags & (1 << RATEEST_OPT_NAME)))
		xtables_error(PARAMETER_PROBLEM, "RATEEST: no name specified");
	if (!(flags & (1 << RATEEST_OPT_INTERVAL)))
		xtables_error(PARAMETER_PROBLEM, "RATEEST: no interval specified");
	if (!(flags & (1 << RATEEST_OPT_EWMALOG)))
		xtables_error(PARAMETER_PROBLEM, "RATEEST: no ewmalog specified");

	for (info->interval = 0; info->interval <= 5; info->interval++) {
		if (interval <= (1 << info->interval) * (TIME_UNITS_PER_SEC / 4))
			break;
	}

	if (info->interval > 5)
		xtables_error(PARAMETER_PROBLEM,
			   "RATEEST: interval value is too large");
	info->interval -= 2;

	for (info->ewma_log = 1; info->ewma_log < 32; info->ewma_log++) {
		double w = 1.0 - 1.0 / (1 << info->ewma_log);
		if (interval / (-log(w)) > ewma_log)
			break;
	}
	info->ewma_log--;

	if (info->ewma_log == 0 || info->ewma_log >= 31)
		xtables_error(PARAMETER_PROBLEM,
			   "RATEEST: ewmalog value is out of range");
}

static void
__RATEEST_print(const struct xt_entry_target *target, const char *prefix)
{
	const struct xt_rateest_target_info *info = (const void *)target->data;
	unsigned int local_interval;
	unsigned int local_ewma_log;

	local_interval = (TIME_UNITS_PER_SEC << (info->interval + 2)) / 4;
	local_ewma_log = local_interval * (1 << (info->ewma_log));

	printf("%sname %s ", prefix, info->name);
	printf("%sinterval ", prefix);
	RATEEST_print_time(local_interval);
	printf("%sewmalog ", prefix);
	RATEEST_print_time(local_ewma_log);
}

static void
RATEEST_print(const void *ip, const struct xt_entry_target *target,
	      int numeric)
{
	__RATEEST_print(target, "");
}

static void
RATEEST_save(const void *ip, const struct xt_entry_target *target)
{
	__RATEEST_print(target, "--rateest-");
}

static struct xtables_target rateest_tg_reg = {
	.family		= NFPROTO_UNSPEC,
	.name		= "RATEEST",
	.version	= XTABLES_VERSION,
	.size		= XT_ALIGN(sizeof(struct xt_rateest_target_info)),
	.userspacesize	= XT_ALIGN(sizeof(struct xt_rateest_target_info)),
	.help		= RATEEST_help,
	.init		= RATEEST_init,
	.parse		= RATEEST_parse,
	.final_check	= RATEEST_final_check,
	.print		= RATEEST_print,
	.save		= RATEEST_save,
	.extra_opts	= RATEEST_opts,
};

void libxt_RATEEST_init(void)
{
	xtables_register_target(&rateest_tg_reg);
}
