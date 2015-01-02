/* Shared library add-on to iptables to add MARK target support. */
#include <stdbool.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <getopt.h>

#include <xtables.h>
#include <linux/netfilter/x_tables.h>
#include <linux/netfilter/xt_MARK.h>

/* Version 0 */
struct xt_mark_target_info {
	unsigned long mark;
};

/* Version 1 */
enum {
	XT_MARK_SET=0,
	XT_MARK_AND,
	XT_MARK_OR,
};

struct xt_mark_target_info_v1 {
	unsigned long mark;
	u_int8_t mode;
};

enum {
	F_MARK = 1 << 0,
};

static void MARK_help(void)
{
	printf(
"MARK target options:\n"
"  --set-mark value                   Set nfmark value\n"
"  --and-mark value                   Binary AND the nfmark with value\n"
"  --or-mark  value                   Binary OR  the nfmark with value\n");
}

static const struct option MARK_opts[] = {
	{.name = "set-mark", .has_arg = true, .val = '1'},
	{.name = "and-mark", .has_arg = true, .val = '2'},
	{.name = "or-mark",  .has_arg = true, .val = '3'},
	XT_GETOPT_TABLEEND,
};

static const struct option mark_tg_opts[] = {
	{.name = "set-xmark", .has_arg = true, .val = 'X'},
	{.name = "set-mark",  .has_arg = true, .val = '='},
	{.name = "and-mark",  .has_arg = true, .val = '&'},
	{.name = "or-mark",   .has_arg = true, .val = '|'},
	{.name = "xor-mark",  .has_arg = true, .val = '^'},
	XT_GETOPT_TABLEEND,
};

static void mark_tg_help(void)
{
	printf(
"MARK target options:\n"
"  --set-xmark value[/mask]  Clear bits in mask and XOR value into nfmark\n"
"  --set-mark value[/mask]   Clear bits in mask and OR value into nfmark\n"
"  --and-mark bits           Binary AND the nfmark with bits\n"
"  --or-mark bits            Binary OR the nfmark with bits\n"
"  --xor-mask bits           Binary XOR the nfmark with bits\n"
"\n");
}

/* Function which parses command options; returns true if it
   ate an option */
static int
MARK_parse_v0(int c, char **argv, int invert, unsigned int *flags,
              const void *entry, struct xt_entry_target **target)
{
	struct xt_mark_target_info *markinfo
		= (struct xt_mark_target_info *)(*target)->data;
	unsigned int mark = 0;

	switch (c) {
	case '1':
		if (!xtables_strtoui(optarg, NULL, &mark, 0, UINT32_MAX))
			xtables_error(PARAMETER_PROBLEM, "Bad MARK value \"%s\"", optarg);
		markinfo->mark = mark;
		if (*flags)
			xtables_error(PARAMETER_PROBLEM,
			           "MARK target: Can't specify --set-mark twice");
		*flags = 1;
		break;
	case '2':
		xtables_error(PARAMETER_PROBLEM,
			   "MARK target: kernel too old for --and-mark");
	case '3':
		xtables_error(PARAMETER_PROBLEM,
			   "MARK target: kernel too old for --or-mark");
	default:
		return 0;
	}

	return 1;
}

static void MARK_check(unsigned int flags)
{
	if (!flags)
		xtables_error(PARAMETER_PROBLEM,
		           "MARK target: Parameter --set/and/or-mark"
			   " is required");
}

static int
MARK_parse_v1(int c, char **argv, int invert, unsigned int *flags,
              const void *entry, struct xt_entry_target **target)
{
	struct xt_mark_target_info_v1 *markinfo
		= (struct xt_mark_target_info_v1 *)(*target)->data;
	unsigned int mark = 0;

	switch (c) {
	case '1':
	        markinfo->mode = XT_MARK_SET;
		break;
	case '2':
	        markinfo->mode = XT_MARK_AND;
		break;
	case '3':
	        markinfo->mode = XT_MARK_OR;
		break;
	default:
		return 0;
	}

	if (!xtables_strtoui(optarg, NULL, &mark, 0, UINT32_MAX))
		xtables_error(PARAMETER_PROBLEM, "Bad MARK value \"%s\"", optarg);
	markinfo->mark = mark;
	if (*flags)
		xtables_error(PARAMETER_PROBLEM,
			   "MARK target: Can't specify --set-mark twice");

	*flags = 1;
	return 1;
}

static int mark_tg_parse(int c, char **argv, int invert, unsigned int *flags,
                         const void *entry, struct xt_entry_target **target)
{
	struct xt_mark_tginfo2 *info = (void *)(*target)->data;
	unsigned int value, mask = UINT32_MAX;
	char *end;

	switch (c) {
	case 'X': /* --set-xmark */
	case '=': /* --set-mark */
		xtables_param_act(XTF_ONE_ACTION, "MARK", *flags & F_MARK);
		xtables_param_act(XTF_NO_INVERT, "MARK", "--set-xmark/--set-mark", invert);
		if (!xtables_strtoui(optarg, &end, &value, 0, UINT32_MAX))
			xtables_param_act(XTF_BAD_VALUE, "MARK", "--set-xmark/--set-mark", optarg);
		if (*end == '/')
			if (!xtables_strtoui(end + 1, &end, &mask, 0, UINT32_MAX))
				xtables_param_act(XTF_BAD_VALUE, "MARK", "--set-xmark/--set-mark", optarg);
		if (*end != '\0')
			xtables_param_act(XTF_BAD_VALUE, "MARK", "--set-xmark/--set-mark", optarg);
		info->mark = value;
		info->mask = mask;

		if (c == '=')
			info->mask = value | mask;
		break;

	case '&': /* --and-mark */
		xtables_param_act(XTF_ONE_ACTION, "MARK", *flags & F_MARK);
		xtables_param_act(XTF_NO_INVERT, "MARK", "--and-mark", invert);
		if (!xtables_strtoui(optarg, NULL, &mask, 0, UINT32_MAX))
			xtables_param_act(XTF_BAD_VALUE, "MARK", "--and-mark", optarg);
		info->mark = 0;
		info->mask = ~mask;
		break;

	case '|': /* --or-mark */
		xtables_param_act(XTF_ONE_ACTION, "MARK", *flags & F_MARK);
		xtables_param_act(XTF_NO_INVERT, "MARK", "--or-mark", invert);
		if (!xtables_strtoui(optarg, NULL, &value, 0, UINT32_MAX))
			xtables_param_act(XTF_BAD_VALUE, "MARK", "--or-mark", optarg);
		info->mark = value;
		info->mask = value;
		break;

	case '^': /* --xor-mark */
		xtables_param_act(XTF_ONE_ACTION, "MARK", *flags & F_MARK);
		xtables_param_act(XTF_NO_INVERT, "MARK", "--xor-mark", invert);
		if (!xtables_strtoui(optarg, NULL, &value, 0, UINT32_MAX))
			xtables_param_act(XTF_BAD_VALUE, "MARK", "--xor-mark", optarg);
		info->mark = value;
		info->mask = 0;
		break;

	default:
		return false;
	}

	*flags |= F_MARK;
	return true;
}

static void mark_tg_check(unsigned int flags)
{
	if (flags == 0)
		xtables_error(PARAMETER_PROBLEM, "MARK: One of the --set-xmark, "
		           "--{and,or,xor,set}-mark options is required");
}

static void
print_mark(unsigned long mark)
{
	printf("0x%lx ", mark);
}

static void MARK_print_v0(const void *ip,
                          const struct xt_entry_target *target, int numeric)
{
	const struct xt_mark_target_info *markinfo =
		(const struct xt_mark_target_info *)target->data;
	printf("MARK set ");
	print_mark(markinfo->mark);
}

static void MARK_save_v0(const void *ip, const struct xt_entry_target *target)
{
	const struct xt_mark_target_info *markinfo =
		(const struct xt_mark_target_info *)target->data;

	printf("--set-mark ");
	print_mark(markinfo->mark);
}

static void MARK_print_v1(const void *ip, const struct xt_entry_target *target,
                          int numeric)
{
	const struct xt_mark_target_info_v1 *markinfo =
		(const struct xt_mark_target_info_v1 *)target->data;

	switch (markinfo->mode) {
	case XT_MARK_SET:
		printf("MARK set ");
		break;
	case XT_MARK_AND:
		printf("MARK and ");
		break;
	case XT_MARK_OR: 
		printf("MARK or ");
		break;
	}
	print_mark(markinfo->mark);
}

static void mark_tg_print(const void *ip, const struct xt_entry_target *target,
                          int numeric)
{
	const struct xt_mark_tginfo2 *info = (const void *)target->data;

	if (info->mark == 0)
		printf("MARK and 0x%x ", (unsigned int)(u_int32_t)~info->mask);
	else if (info->mark == info->mask)
		printf("MARK or 0x%x ", info->mark);
	else if (info->mask == 0)
		printf("MARK xor 0x%x ", info->mark);
	else if (info->mask == 0xffffffffU)
		printf("MARK set 0x%x ", info->mark);
	else
		printf("MARK xset 0x%x/0x%x ", info->mark, info->mask);
}

static void MARK_save_v1(const void *ip, const struct xt_entry_target *target)
{
	const struct xt_mark_target_info_v1 *markinfo =
		(const struct xt_mark_target_info_v1 *)target->data;

	switch (markinfo->mode) {
	case XT_MARK_SET:
		printf("--set-mark ");
		break;
	case XT_MARK_AND:
		printf("--and-mark ");
		break;
	case XT_MARK_OR: 
		printf("--or-mark ");
		break;
	}
	print_mark(markinfo->mark);
}

static void mark_tg_save(const void *ip, const struct xt_entry_target *target)
{
	const struct xt_mark_tginfo2 *info = (const void *)target->data;

	printf("--set-xmark 0x%x/0x%x ", info->mark, info->mask);
}

static struct xtables_target mark_tg_reg[] = {
	{
		.family        = NFPROTO_UNSPEC,
		.name          = "MARK",
		.version       = XTABLES_VERSION,
		.revision      = 0,
		.size          = XT_ALIGN(sizeof(struct xt_mark_target_info)),
		.userspacesize = XT_ALIGN(sizeof(struct xt_mark_target_info)),
		.help          = MARK_help,
		.parse         = MARK_parse_v0,
		.final_check   = MARK_check,
		.print         = MARK_print_v0,
		.save          = MARK_save_v0,
		.extra_opts    = MARK_opts,
	},
	{
		.family        = NFPROTO_IPV4,
		.name          = "MARK",
		.version       = XTABLES_VERSION,
		.revision      = 1,
		.size          = XT_ALIGN(sizeof(struct xt_mark_target_info_v1)),
		.userspacesize = XT_ALIGN(sizeof(struct xt_mark_target_info_v1)),
		.help          = MARK_help,
		.parse         = MARK_parse_v1,
		.final_check   = MARK_check,
		.print         = MARK_print_v1,
		.save          = MARK_save_v1,
		.extra_opts    = MARK_opts,
	},
	{
		.version       = XTABLES_VERSION,
		.name          = "MARK",
		.revision      = 2,
		.family        = NFPROTO_UNSPEC,
		.size          = XT_ALIGN(sizeof(struct xt_mark_tginfo2)),
		.userspacesize = XT_ALIGN(sizeof(struct xt_mark_tginfo2)),
		.help          = mark_tg_help,
		.parse         = mark_tg_parse,
		.final_check   = mark_tg_check,
		.print         = mark_tg_print,
		.save          = mark_tg_save,
		.extra_opts    = mark_tg_opts,
	},
};

void libxt_MARK_init(void)
{
	xtables_register_targets(mark_tg_reg, ARRAY_SIZE(mark_tg_reg));
}
