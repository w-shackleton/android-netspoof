/* Shared library add-on to iptables for DSCP
 *
 * (C) 2000- 2002 by Matthew G. Marsh <mgm@paktronix.com>,
 * 		     Harald Welte <laforge@gnumonks.org>
 *
 * This program is distributed under the terms of GNU GPL v2, 1991
 *
 * libipt_DSCP.c borrowed heavily from libipt_TOS.c
 *
 * --set-class added by Iain Barnes
 */
#include <stdbool.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <getopt.h>

#include <xtables.h>
#include <linux/netfilter/x_tables.h>
#include <linux/netfilter/xt_DSCP.h>

/* This is evil, but it's my code - HW*/
#include "dscp_helper.c"

static void DSCP_help(void)
{
	printf(
"DSCP target options\n"
"  --set-dscp value		Set DSCP field in packet header to value\n"
"  		                This value can be in decimal (ex: 32)\n"
"               		or in hex (ex: 0x20)\n"
"  --set-dscp-class class	Set the DSCP field in packet header to the\n"
"				value represented by the DiffServ class value.\n"
"				This class may be EF,BE or any of the CSxx\n"
"				or AFxx classes.\n"
"\n"
"				These two options are mutually exclusive !\n"
);
}

static const struct option DSCP_opts[] = {
	{.name = "set-dscp",       .has_arg = true, .val = 'F'},
	{.name = "set-dscp-class", .has_arg = true, .val = 'G'},
	XT_GETOPT_TABLEEND,
};

static void
parse_dscp(const char *s, struct xt_DSCP_info *dinfo)
{
	unsigned int dscp;
       
	if (!xtables_strtoui(s, NULL, &dscp, 0, UINT8_MAX))
		xtables_error(PARAMETER_PROBLEM,
			   "Invalid dscp `%s'\n", s);

	if (dscp > XT_DSCP_MAX)
		xtables_error(PARAMETER_PROBLEM,
			   "DSCP `%d` out of range\n", dscp);

	dinfo->dscp = dscp;
}


static void
parse_class(const char *s, struct xt_DSCP_info *dinfo)
{
	unsigned int dscp = class_to_dscp(s);

	/* Assign the value */
	dinfo->dscp = dscp;
}


static int DSCP_parse(int c, char **argv, int invert, unsigned int *flags,
                      const void *entry, struct xt_entry_target **target)
{
	struct xt_DSCP_info *dinfo
		= (struct xt_DSCP_info *)(*target)->data;

	switch (c) {
	case 'F':
		if (*flags)
			xtables_error(PARAMETER_PROBLEM,
			           "DSCP target: Only use --set-dscp ONCE!");
		parse_dscp(optarg, dinfo);
		*flags = 1;
		break;
	case 'G':
		if (*flags)
			xtables_error(PARAMETER_PROBLEM,
				   "DSCP target: Only use --set-dscp-class ONCE!");
		parse_class(optarg, dinfo);
		*flags = 1;
		break;

	default:
		return 0;
	}

	return 1;
}

static void DSCP_check(unsigned int flags)
{
	if (!flags)
		xtables_error(PARAMETER_PROBLEM,
		           "DSCP target: Parameter --set-dscp is required");
}

static void
print_dscp(u_int8_t dscp, int numeric)
{
 	printf("0x%02x ", dscp);
}

static void DSCP_print(const void *ip, const struct xt_entry_target *target,
                       int numeric)
{
	const struct xt_DSCP_info *dinfo =
		(const struct xt_DSCP_info *)target->data;
	printf("DSCP set ");
	print_dscp(dinfo->dscp, numeric);
}

static void DSCP_save(const void *ip, const struct xt_entry_target *target)
{
	const struct xt_DSCP_info *dinfo =
		(const struct xt_DSCP_info *)target->data;

	printf("--set-dscp 0x%02x ", dinfo->dscp);
}

static struct xtables_target dscp_target = {
	.family		= NFPROTO_UNSPEC,
	.name		= "DSCP",
	.version	= XTABLES_VERSION,
	.size		= XT_ALIGN(sizeof(struct xt_DSCP_info)),
	.userspacesize	= XT_ALIGN(sizeof(struct xt_DSCP_info)),
	.help		= DSCP_help,
	.parse		= DSCP_parse,
	.final_check	= DSCP_check,
	.print		= DSCP_print,
	.save		= DSCP_save,
	.extra_opts	= DSCP_opts,
};

void libxt_DSCP_init(void)
{
	xtables_register_target(&dscp_target);
}
