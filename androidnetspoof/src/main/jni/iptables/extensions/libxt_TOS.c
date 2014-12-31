/*
 * Shared library add-on to iptables to add TOS target support
 *
 * Copyright © CC Computer Consultants GmbH, 2007
 * Contact: Jan Engelhardt <jengelh@computergmbh.de>
 */
#include <getopt.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <netinet/in.h>

#include <xtables.h>
#include <linux/netfilter/xt_DSCP.h>
#include "tos_values.c"

struct ipt_tos_target_info {
	u_int8_t tos;
};

enum {
	FLAG_TOS = 1 << 0,
};

static const struct option tos_tg_opts_v0[] = {
	{.name = "set-tos", .has_arg = true, .val = '='},
	XT_GETOPT_TABLEEND,
};

static const struct option tos_tg_opts[] = {
	{.name = "set-tos", .has_arg = true, .val = '='},
	{.name = "and-tos", .has_arg = true, .val = '&'},
	{.name = "or-tos",  .has_arg = true, .val = '|'},
	{.name = "xor-tos", .has_arg = true, .val = '^'},
	XT_GETOPT_TABLEEND,
};

static void tos_tg_help_v0(void)
{
	const struct tos_symbol_info *symbol;

	printf(
"TOS target options:\n"
"  --set-tos value     Set Type of Service/Priority field to value\n"
"  --set-tos symbol    Set TOS field (IPv4 only) by symbol\n"
"                      Accepted symbolic names for value are:\n");

	for (symbol = tos_symbol_names; symbol->name != NULL; ++symbol)
		printf("                        (0x%02x) %2u %s\n",
		       symbol->value, symbol->value, symbol->name);

	printf("\n");
}

static void tos_tg_help(void)
{
	const struct tos_symbol_info *symbol;

	printf(
"TOS target v%s options:\n"
"  --set-tos value[/mask]  Set Type of Service/Priority field to value\n"
"                          (Zero out bits in mask and XOR value into TOS)\n"
"  --set-tos symbol        Set TOS field (IPv4 only) by symbol\n"
"                          (this zeroes the 4-bit Precedence part!)\n"
"                          Accepted symbolic names for value are:\n",
XTABLES_VERSION);

	for (symbol = tos_symbol_names; symbol->name != NULL; ++symbol)
		printf("                            (0x%02x) %2u %s\n",
		       symbol->value, symbol->value, symbol->name);

	printf(
"\n"
"  --and-tos bits          Binary AND the TOS value with bits\n"
"  --or-tos  bits          Binary OR the TOS value with bits\n"
"  --xor-tos bits          Binary XOR the TOS value with bits\n"
);
}

static int tos_tg_parse_v0(int c, char **argv, int invert, unsigned int *flags,
                           const void *entry, struct xt_entry_target **target)
{
	struct ipt_tos_target_info *info = (void *)(*target)->data;
	struct tos_value_mask tvm;

	switch (c) {
	case '=':
		xtables_param_act(XTF_ONLY_ONCE, "TOS", "--set-tos", *flags & FLAG_TOS);
		xtables_param_act(XTF_NO_INVERT, "TOS", "--set-tos", invert);
		if (!tos_parse_symbolic(optarg, &tvm, 0xFF))
			xtables_param_act(XTF_BAD_VALUE, "TOS", "--set-tos", optarg);
		if (tvm.mask != 0xFF)
			xtables_error(PARAMETER_PROBLEM, "tos match: Your kernel "
			           "is too old to support anything besides "
				   "/0xFF as a mask.");
		info->tos = tvm.value;
		*flags |= FLAG_TOS;
		return true;
	}

	return false;
}

static int tos_tg_parse(int c, char **argv, int invert, unsigned int *flags,
                         const void *entry, struct xt_entry_target **target)
{
	struct xt_tos_target_info *info = (void *)(*target)->data;
	struct tos_value_mask tvm;
	unsigned int bits;

	switch (c) {
	case '=': /* --set-tos */
		xtables_param_act(XTF_ONLY_ONCE, "TOS", "--set-tos", *flags & FLAG_TOS);
		xtables_param_act(XTF_NO_INVERT, "TOS", "--set-tos", invert);
		if (!tos_parse_symbolic(optarg, &tvm, 0x3F))
			xtables_param_act(XTF_BAD_VALUE, "TOS", "--set-tos", optarg);
		info->tos_value = tvm.value;
		info->tos_mask  = tvm.mask;
		break;

	case '&': /* --and-tos */
		xtables_param_act(XTF_ONLY_ONCE, "TOS", "--and-tos", *flags & FLAG_TOS);
		xtables_param_act(XTF_NO_INVERT, "TOS", "--and-tos", invert);
		if (!xtables_strtoui(optarg, NULL, &bits, 0, UINT8_MAX))
			xtables_param_act(XTF_BAD_VALUE, "TOS", "--and-tos", optarg);
		info->tos_value = 0;
		info->tos_mask  = ~bits;
		break;

	case '|': /* --or-tos */
		xtables_param_act(XTF_ONLY_ONCE, "TOS", "--or-tos", *flags & FLAG_TOS);
		xtables_param_act(XTF_NO_INVERT, "TOS", "--or-tos", invert);
		if (!xtables_strtoui(optarg, NULL, &bits, 0, UINT8_MAX))
			xtables_param_act(XTF_BAD_VALUE, "TOS", "--or-tos", optarg);
		info->tos_value = bits;
		info->tos_mask  = bits;
		break;

	case '^': /* --xor-tos */
		xtables_param_act(XTF_ONLY_ONCE, "TOS", "--xor-tos", *flags & FLAG_TOS);
		xtables_param_act(XTF_NO_INVERT, "TOS", "--xor-tos", invert);
		if (!xtables_strtoui(optarg, NULL, &bits, 0, UINT8_MAX))
			xtables_param_act(XTF_BAD_VALUE, "TOS", "--xor-tos", optarg);
		info->tos_value = bits;
		info->tos_mask  = 0;
		break;

	default:
		return false;
	}

	*flags |= FLAG_TOS;
	return true;
}

static void tos_tg_check(unsigned int flags)
{
	if (flags == 0)
		xtables_error(PARAMETER_PROBLEM,
		           "TOS: The --set-tos parameter is required");
}

static void tos_tg_print_v0(const void *ip,
                            const struct xt_entry_target *target, int numeric)
{
	const struct ipt_tos_target_info *info = (const void *)target->data;

	printf("TOS set ");
	if (numeric || !tos_try_print_symbolic("", info->tos, 0xFF))
		printf("0x%02x ", info->tos);
}

static void tos_tg_print(const void *ip, const struct xt_entry_target *target,
                         int numeric)
{
	const struct xt_tos_target_info *info = (const void *)target->data;

	if (numeric)
		printf("TOS set 0x%02x/0x%02x ",
		       info->tos_value, info->tos_mask);
	else if (tos_try_print_symbolic("TOS set ",
	    info->tos_value, info->tos_mask))
		/* already printed by call */
		return;
	else if (info->tos_value == 0)
		printf("TOS and 0x%02x ",
		       (unsigned int)(u_int8_t)~info->tos_mask);
	else if (info->tos_value == info->tos_mask)
		printf("TOS or 0x%02x ", info->tos_value);
	else if (info->tos_mask == 0)
		printf("TOS xor 0x%02x ", info->tos_value);
	else
		printf("TOS set 0x%02x/0x%02x ",
		       info->tos_value, info->tos_mask);
}

static void tos_tg_save_v0(const void *ip, const struct xt_entry_target *target)
{
	const struct ipt_tos_target_info *info = (const void *)target->data;

	printf("--set-tos 0x%02x ", info->tos);
}

static void tos_tg_save(const void *ip, const struct xt_entry_target *target)
{
	const struct xt_tos_target_info *info = (const void *)target->data;

	printf("--set-tos 0x%02x/0x%02x ", info->tos_value, info->tos_mask);
}

static struct xtables_target tos_tg_reg[] = {
	{
		.version       = XTABLES_VERSION,
		.name          = "TOS",
		.revision      = 0,
		.family        = NFPROTO_IPV4,
		.size          = XT_ALIGN(sizeof(struct xt_tos_target_info)),
		.userspacesize = XT_ALIGN(sizeof(struct xt_tos_target_info)),
		.help          = tos_tg_help_v0,
		.parse         = tos_tg_parse_v0,
		.final_check   = tos_tg_check,
		.print         = tos_tg_print_v0,
		.save          = tos_tg_save_v0,
		.extra_opts    = tos_tg_opts_v0,
	},
	{
		.version       = XTABLES_VERSION,
		.name          = "TOS",
		.revision      = 1,
		.family        = NFPROTO_UNSPEC,
		.size          = XT_ALIGN(sizeof(struct xt_tos_target_info)),
		.userspacesize = XT_ALIGN(sizeof(struct xt_tos_target_info)),
		.help          = tos_tg_help,
		.parse         = tos_tg_parse,
		.final_check   = tos_tg_check,
		.print         = tos_tg_print,
		.save          = tos_tg_save,
		.extra_opts    = tos_tg_opts,
	},
};

void libxt_TOS_init(void)
{
	xtables_register_targets(tos_tg_reg, ARRAY_SIZE(tos_tg_reg));
}
