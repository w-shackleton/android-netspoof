/* Shared library add-on to iptables to add TCPMSS target support.
 *
 * Copyright (c) 2000 Marc Boucher
*/
#include <stdbool.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <getopt.h>

#include <xtables.h>
#include <linux/netfilter/x_tables.h>
#include <linux/netfilter/xt_TCPMSS.h>

struct mssinfo {
	struct xt_entry_target t;
	struct xt_tcpmss_info mss;
};

static void __TCPMSS_help(int hdrsize)
{
	printf(
"TCPMSS target mutually-exclusive options:\n"
"  --set-mss value               explicitly set MSS option to specified value\n"
"  --clamp-mss-to-pmtu           automatically clamp MSS value to (path_MTU - %d)\n",
hdrsize);
}

static void TCPMSS_help(void)
{
	__TCPMSS_help(40);
}

static void TCPMSS_help6(void)
{
	__TCPMSS_help(60);
}

static const struct option TCPMSS_opts[] = {
	{.name = "set-mss",           .has_arg = true,  .val = '1'},
	{.name = "clamp-mss-to-pmtu", .has_arg = false, .val = '2'},
	XT_GETOPT_TABLEEND,
};

static int __TCPMSS_parse(int c, char **argv, int invert, unsigned int *flags,
                          const void *entry, struct xt_entry_target **target,
                          int hdrsize)
{
	struct xt_tcpmss_info *mssinfo
		= (struct xt_tcpmss_info *)(*target)->data;

	switch (c) {
		unsigned int mssval;

	case '1':
		if (*flags)
			xtables_error(PARAMETER_PROBLEM,
			           "TCPMSS target: Only one option may be specified");
		if (!xtables_strtoui(optarg, NULL, &mssval,
		    0, UINT16_MAX - hdrsize))
			xtables_error(PARAMETER_PROBLEM, "Bad TCPMSS value \"%s\"", optarg);
		
		mssinfo->mss = mssval;
		*flags = 1;
		break;

	case '2':
		if (*flags)
			xtables_error(PARAMETER_PROBLEM,
			           "TCPMSS target: Only one option may be specified");
		mssinfo->mss = XT_TCPMSS_CLAMP_PMTU;
		*flags = 1;
		break;

	default:
		return 0;
	}

	return 1;
}

static int TCPMSS_parse(int c, char **argv, int invert, unsigned int *flags,
                        const void *entry, struct xt_entry_target **target)
{
	return __TCPMSS_parse(c, argv, invert, flags, entry, target, 40);
}

static int TCPMSS_parse6(int c, char **argv, int invert, unsigned int *flags,
                         const void *entry, struct xt_entry_target **target)
{
	return __TCPMSS_parse(c, argv, invert, flags, entry, target, 60);
}

static void TCPMSS_check(unsigned int flags)
{
	if (!flags)
		xtables_error(PARAMETER_PROBLEM,
		           "TCPMSS target: At least one parameter is required");
}

static void TCPMSS_print(const void *ip, const struct xt_entry_target *target,
                         int numeric)
{
	const struct xt_tcpmss_info *mssinfo =
		(const struct xt_tcpmss_info *)target->data;
	if(mssinfo->mss == XT_TCPMSS_CLAMP_PMTU)
		printf("TCPMSS clamp to PMTU ");
	else
		printf("TCPMSS set %u ", mssinfo->mss);
}

static void TCPMSS_save(const void *ip, const struct xt_entry_target *target)
{
	const struct xt_tcpmss_info *mssinfo =
		(const struct xt_tcpmss_info *)target->data;

	if(mssinfo->mss == XT_TCPMSS_CLAMP_PMTU)
		printf("--clamp-mss-to-pmtu ");
	else
		printf("--set-mss %u ", mssinfo->mss);
}

static struct xtables_target tcpmss_target = {
	.family		= NFPROTO_IPV4,
	.name		= "TCPMSS",
	.version	= XTABLES_VERSION,
	.size		= XT_ALIGN(sizeof(struct xt_tcpmss_info)),
	.userspacesize	= XT_ALIGN(sizeof(struct xt_tcpmss_info)),
	.help		= TCPMSS_help,
	.parse		= TCPMSS_parse,
	.final_check	= TCPMSS_check,
	.print		= TCPMSS_print,
	.save		= TCPMSS_save,
	.extra_opts	= TCPMSS_opts,
};

static struct xtables_target tcpmss_target6 = {
	.family		= NFPROTO_IPV6,
	.name		= "TCPMSS",
	.version	= XTABLES_VERSION,
	.size		= XT_ALIGN(sizeof(struct xt_tcpmss_info)),
	.userspacesize	= XT_ALIGN(sizeof(struct xt_tcpmss_info)),
	.help		= TCPMSS_help6,
	.parse		= TCPMSS_parse6,
	.final_check	= TCPMSS_check,
	.print		= TCPMSS_print,
	.save		= TCPMSS_save,
	.extra_opts	= TCPMSS_opts,
};

void libxt_TCPMSS_init(void)
{
	xtables_register_target(&tcpmss_target);
	xtables_register_target(&tcpmss_target6);
}
