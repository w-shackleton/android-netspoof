/* Shared library add-on to iptables for ECN, $Version$
 *
 * (C) 2002 by Harald Welte <laforge@gnumonks.org>
 *
 * This program is distributed under the terms of GNU GPL v2, 1991
 *
 * libipt_ECN.c borrowed heavily from libipt_DSCP.c
 *
 * $Id: //atg/packetfilter/tagging/platform_passion/external/iptables/extensions/libipt_ECN.c#1 $
 */
#include <stdbool.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <getopt.h>

#include <xtables.h>
#include <linux/netfilter_ipv4/ipt_ECN.h>

static void ECN_help(void)
{
	printf(
"ECN target options\n"
"  --ecn-tcp-remove		Remove all ECN bits from TCP header\n");
}

#if 0
"ECN target v%s EXPERIMENTAL options (use with extreme care!)\n"
"  --ecn-ip-ect			Set the IPv4 ECT codepoint (0 to 3)\n"
"  --ecn-tcp-cwr		Set the IPv4 CWR bit (0 or 1)\n"
"  --ecn-tcp-ece		Set the IPv4 ECE bit (0 or 1)\n",
#endif


static const struct option ECN_opts[] = {
	{.name = "ecn-tcp-remove", .has_arg = false, .val = 'F'},
	{.name = "ecn-tcp-cwr",    .has_arg = true,  .val = 'G'},
	{.name = "ecn-tcp-ece",    .has_arg = true,  .val = 'H'},
	{.name = "ecn-ip-ect",     .has_arg = true,  .val = '9'},
	XT_GETOPT_TABLEEND,
};

static int ECN_parse(int c, char **argv, int invert, unsigned int *flags,
                     const void *entry, struct xt_entry_target **target)
{
	unsigned int result;
	struct ipt_ECN_info *einfo
		= (struct ipt_ECN_info *)(*target)->data;

	switch (c) {
	case 'F':
		if (*flags)
			xtables_error(PARAMETER_PROBLEM,
			        "ECN target: Only use --ecn-tcp-remove ONCE!");
		einfo->operation = IPT_ECN_OP_SET_ECE | IPT_ECN_OP_SET_CWR;
		einfo->proto.tcp.ece = 0;
		einfo->proto.tcp.cwr = 0;
		*flags = 1;
		break;
	case 'G':
		if (*flags & IPT_ECN_OP_SET_CWR)
			xtables_error(PARAMETER_PROBLEM,
				"ECN target: Only use --ecn-tcp-cwr ONCE!");
		if (!xtables_strtoui(optarg, NULL, &result, 0, 1))
			xtables_error(PARAMETER_PROBLEM,
				   "ECN target: Value out of range");
		einfo->operation |= IPT_ECN_OP_SET_CWR;
		einfo->proto.tcp.cwr = result;
		*flags |= IPT_ECN_OP_SET_CWR;
		break;
	case 'H':
		if (*flags & IPT_ECN_OP_SET_ECE)
			xtables_error(PARAMETER_PROBLEM,
				"ECN target: Only use --ecn-tcp-ece ONCE!");
		if (!xtables_strtoui(optarg, NULL, &result, 0, 1))
			xtables_error(PARAMETER_PROBLEM,
				   "ECN target: Value out of range");
		einfo->operation |= IPT_ECN_OP_SET_ECE;
		einfo->proto.tcp.ece = result;
		*flags |= IPT_ECN_OP_SET_ECE;
		break;
	case '9':
		if (*flags & IPT_ECN_OP_SET_IP)
			xtables_error(PARAMETER_PROBLEM,
				"ECN target: Only use --ecn-ip-ect ONCE!");
		if (!xtables_strtoui(optarg, NULL, &result, 0, 3))
			xtables_error(PARAMETER_PROBLEM,
				   "ECN target: Value out of range");
		einfo->operation |= IPT_ECN_OP_SET_IP;
		einfo->ip_ect = result;
		*flags |= IPT_ECN_OP_SET_IP;
		break;
	default:
		return 0;
	}

	return 1;
}

static void ECN_check(unsigned int flags)
{
	if (!flags)
		xtables_error(PARAMETER_PROBLEM,
		           "ECN target: Parameter --ecn-tcp-remove is required");
}

static void ECN_print(const void *ip, const struct xt_entry_target *target,
                      int numeric)
{
	const struct ipt_ECN_info *einfo =
		(const struct ipt_ECN_info *)target->data;

	printf("ECN ");

	if (einfo->operation == (IPT_ECN_OP_SET_ECE|IPT_ECN_OP_SET_CWR)
	    && einfo->proto.tcp.ece == 0
	    && einfo->proto.tcp.cwr == 0)
		printf("TCP remove ");
	else {
		if (einfo->operation & IPT_ECN_OP_SET_ECE)
			printf("ECE=%u ", einfo->proto.tcp.ece);

		if (einfo->operation & IPT_ECN_OP_SET_CWR)
			printf("CWR=%u ", einfo->proto.tcp.cwr);

		if (einfo->operation & IPT_ECN_OP_SET_IP)
			printf("ECT codepoint=%u ", einfo->ip_ect);
	}
}

static void ECN_save(const void *ip, const struct xt_entry_target *target)
{
	const struct ipt_ECN_info *einfo =
		(const struct ipt_ECN_info *)target->data;

	if (einfo->operation == (IPT_ECN_OP_SET_ECE|IPT_ECN_OP_SET_CWR)
	    && einfo->proto.tcp.ece == 0
	    && einfo->proto.tcp.cwr == 0)
		printf("--ecn-tcp-remove ");
	else {

		if (einfo->operation & IPT_ECN_OP_SET_ECE)
			printf("--ecn-tcp-ece %d ", einfo->proto.tcp.ece);

		if (einfo->operation & IPT_ECN_OP_SET_CWR)
			printf("--ecn-tcp-cwr %d ", einfo->proto.tcp.cwr);

		if (einfo->operation & IPT_ECN_OP_SET_IP)
			printf("--ecn-ip-ect %d ", einfo->ip_ect);
	}
}

static struct xtables_target ecn_tg_reg = {
	.name		= "ECN",
	.version	= XTABLES_VERSION,
	.family		= NFPROTO_IPV4,
	.size		= XT_ALIGN(sizeof(struct ipt_ECN_info)),
	.userspacesize	= XT_ALIGN(sizeof(struct ipt_ECN_info)),
	.help		= ECN_help,
	.parse		= ECN_parse,
	.final_check	= ECN_check,
	.print		= ECN_print,
	.save		= ECN_save,
	.extra_opts	= ECN_opts,
};

void libipt_ECN_init(void)
{
	xtables_register_target(&ecn_tg_reg);
}
