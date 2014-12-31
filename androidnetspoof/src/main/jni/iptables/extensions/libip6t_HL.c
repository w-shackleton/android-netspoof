/*
 * IPv6 Hop Limit Target module
 * Maciej Soltysiak <solt@dns.toxicfilms.tv>
 * Based on HW's ttl target
 * This program is distributed under the terms of GNU GPL
 */

#include <getopt.h>
#include <stdbool.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <xtables.h>

#include <linux/netfilter_ipv6/ip6t_HL.h>

#define IP6T_HL_USED	1

static void HL_help(void)
{
	printf(
"HL target options\n"
"  --hl-set value		Set HL to <value 0-255>\n"
"  --hl-dec value		Decrement HL by <value 1-255>\n"
"  --hl-inc value		Increment HL by <value 1-255>\n");
}

static int HL_parse(int c, char **argv, int invert, unsigned int *flags,
                    const void *entry, struct xt_entry_target **target)
{
	struct ip6t_HL_info *info = (struct ip6t_HL_info *) (*target)->data;
	unsigned int value;

	if (*flags & IP6T_HL_USED) {
		xtables_error(PARAMETER_PROBLEM,
				"Can't specify HL option twice");
	}

	if (!optarg) 
		xtables_error(PARAMETER_PROBLEM,
				"HL: You must specify a value");

	if (xtables_check_inverse(optarg, &invert, NULL, 0, argv))
		xtables_error(PARAMETER_PROBLEM,
				"HL: unexpected `!'");
	
	if (!xtables_strtoui(optarg, NULL, &value, 0, UINT8_MAX))
		xtables_error(PARAMETER_PROBLEM,
		           "HL: Expected value between 0 and 255");

	switch (c) {

		case '1':
			info->mode = IP6T_HL_SET;
			break;

		case '2':
			if (value == 0) {
				xtables_error(PARAMETER_PROBLEM,
					"HL: decreasing by 0?");
			}

			info->mode = IP6T_HL_DEC;
			break;

		case '3':
			if (value == 0) {
				xtables_error(PARAMETER_PROBLEM,
					"HL: increasing by 0?");
			}

			info->mode = IP6T_HL_INC;
			break;

		default:
			return 0;

	}
	
	info->hop_limit = value;
	*flags |= IP6T_HL_USED;

	return 1;
}

static void HL_check(unsigned int flags)
{
	if (!(flags & IP6T_HL_USED))
		xtables_error(PARAMETER_PROBLEM,
				"HL: You must specify an action");
}

static void HL_save(const void *ip, const struct xt_entry_target *target)
{
	const struct ip6t_HL_info *info = 
		(struct ip6t_HL_info *) target->data;

	switch (info->mode) {
		case IP6T_HL_SET:
			printf("--hl-set ");
			break;
		case IP6T_HL_DEC:
			printf("--hl-dec ");
			break;

		case IP6T_HL_INC:
			printf("--hl-inc ");
			break;
	}
	printf("%u ", info->hop_limit);
}

static void HL_print(const void *ip, const struct xt_entry_target *target,
                     int numeric)
{
	const struct ip6t_HL_info *info =
		(struct ip6t_HL_info *) target->data;

	printf("HL ");
	switch (info->mode) {
		case IP6T_HL_SET:
			printf("set to ");
			break;
		case IP6T_HL_DEC:
			printf("decrement by ");
			break;
		case IP6T_HL_INC:
			printf("increment by ");
			break;
	}
	printf("%u ", info->hop_limit);
}

static const struct option HL_opts[] = {
	{.name = "hl-set", .has_arg = true, .val = '1'},
	{.name = "hl-dec", .has_arg = true, .val = '2'},
	{.name = "hl-inc", .has_arg = true, .val = '3'},
	XT_GETOPT_TABLEEND,
};

static struct xtables_target hl_tg6_reg = {
	.name 		= "HL",
	.version	= XTABLES_VERSION,
	.family		= NFPROTO_IPV6,
	.size		= XT_ALIGN(sizeof(struct ip6t_HL_info)),
	.userspacesize	= XT_ALIGN(sizeof(struct ip6t_HL_info)),
	.help		= HL_help,
	.parse		= HL_parse,
	.final_check	= HL_check,
	.print		= HL_print,
	.save		= HL_save,
	.extra_opts	= HL_opts,
};

void _init(void)
{
	xtables_register_target(&hl_tg6_reg);
}
