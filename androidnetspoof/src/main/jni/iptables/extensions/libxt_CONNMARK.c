/* Shared library add-on to iptables to add CONNMARK target support.
 *
 * (C) 2002,2004 MARA Systems AB <http://www.marasystems.com>
 * by Henrik Nordstrom <hno@marasystems.com>
 *
 * Version 1.1
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
#include <stdbool.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <getopt.h>

#include <xtables.h>
#include <linux/netfilter/x_tables.h>
#include <linux/netfilter/xt_CONNMARK.h>

struct xt_connmark_target_info {
	unsigned long mark;
	unsigned long mask;
	u_int8_t mode;
};

enum {
	F_MARK    = 1 << 0,
	F_SR_MARK = 1 << 1,
};

static void CONNMARK_help(void)
{
	printf(
"CONNMARK target options:\n"
"  --set-mark value[/mask]       Set conntrack mark value\n"
"  --save-mark [--mask mask]     Save the packet nfmark in the connection\n"
"  --restore-mark [--mask mask]  Restore saved nfmark value\n");
}

static const struct option CONNMARK_opts[] = {
	{.name = "set-mark",     .has_arg = true,  .val = '1'},
	{.name = "save-mark",    .has_arg = false, .val = '2'},
	{.name = "restore-mark", .has_arg = false, .val = '3'},
	{.name = "mask",         .has_arg = true,  .val = '4'},
	XT_GETOPT_TABLEEND,
};

static const struct option connmark_tg_opts[] = {
	{.name = "set-xmark",     .has_arg = true,  .val = '='},
	{.name = "set-mark",      .has_arg = true,  .val = '-'},
	{.name = "and-mark",      .has_arg = true,  .val = '&'},
	{.name = "or-mark",       .has_arg = true,  .val = '|'},
	{.name = "xor-mark",      .has_arg = true,  .val = '^'},
	{.name = "save-mark",     .has_arg = false, .val = 'S'},
	{.name = "restore-mark",  .has_arg = false, .val = 'R'},
	{.name = "ctmask",        .has_arg = true,  .val = 'c'},
	{.name = "nfmask",        .has_arg = true,  .val = 'n'},
	{.name = "mask",          .has_arg = true,  .val = 'm'},
	XT_GETOPT_TABLEEND,
};

static void connmark_tg_help(void)
{
	printf(
"CONNMARK target options:\n"
"  --set-xmark value[/ctmask]    Zero mask bits and XOR ctmark with value\n"
"  --save-mark [--ctmask mask] [--nfmask mask]\n"
"                                Copy ctmark to nfmark using masks\n"
"  --restore-mark [--ctmask mask] [--nfmask mask]\n"
"                                Copy nfmark to ctmark using masks\n"
"  --set-mark value[/mask]       Set conntrack mark value\n"
"  --save-mark [--mask mask]     Save the packet nfmark in the connection\n"
"  --restore-mark [--mask mask]  Restore saved nfmark value\n"
"  --and-mark value              Binary AND the ctmark with bits\n"
"  --or-mark value               Binary OR  the ctmark with bits\n"
"  --xor-mark value              Binary XOR the ctmark with bits\n"
);
}

static void connmark_tg_init(struct xt_entry_target *target)
{
	struct xt_connmark_tginfo1 *info = (void *)target->data;

	/*
	 * Need these defaults for --save-mark/--restore-mark if no
	 * --ctmark or --nfmask is given.
	 */
	info->ctmask = UINT32_MAX;
	info->nfmask = UINT32_MAX;
}

static int
CONNMARK_parse(int c, char **argv, int invert, unsigned int *flags,
               const void *entry, struct xt_entry_target **target)
{
	struct xt_connmark_target_info *markinfo
		= (struct xt_connmark_target_info *)(*target)->data;

	switch (c) {
		char *end;
	case '1':
		markinfo->mode = XT_CONNMARK_SET;

		markinfo->mark = strtoul(optarg, &end, 0);
		if (*end == '/' && end[1] != '\0')
		    markinfo->mask = strtoul(end+1, &end, 0);

		if (*end != '\0' || end == optarg)
			xtables_error(PARAMETER_PROBLEM, "Bad MARK value \"%s\"", optarg);
		if (*flags)
			xtables_error(PARAMETER_PROBLEM,
			           "CONNMARK target: Can't specify --set-mark twice");
		*flags = 1;
		break;
	case '2':
		markinfo->mode = XT_CONNMARK_SAVE;
		if (*flags)
			xtables_error(PARAMETER_PROBLEM,
			           "CONNMARK target: Can't specify --save-mark twice");
		*flags = 1;
		break;
	case '3':
		markinfo->mode = XT_CONNMARK_RESTORE;
		if (*flags)
			xtables_error(PARAMETER_PROBLEM,
			           "CONNMARK target: Can't specify --restore-mark twice");
		*flags = 1;
		break;
	case '4':
		if (!*flags)
			xtables_error(PARAMETER_PROBLEM,
			           "CONNMARK target: Can't specify --mask without a operation");
		markinfo->mask = strtoul(optarg, &end, 0);

		if (*end != '\0' || end == optarg)
			xtables_error(PARAMETER_PROBLEM, "Bad MASK value \"%s\"", optarg);
		break;
	default:
		return 0;
	}

	return 1;
}

static int connmark_tg_parse(int c, char **argv, int invert,
                             unsigned int *flags, const void *entry,
                             struct xt_entry_target **target)
{
	struct xt_connmark_tginfo1 *info = (void *)(*target)->data;
	unsigned int value, mask = UINT32_MAX;
	char *end;

	switch (c) {
	case '=': /* --set-xmark */
	case '-': /* --set-mark */
		xtables_param_act(XTF_ONE_ACTION, "CONNMARK", *flags & F_MARK);
		if (!xtables_strtoui(optarg, &end, &value, 0, UINT32_MAX))
			xtables_param_act(XTF_BAD_VALUE, "CONNMARK", "--set-xmark/--set-mark", optarg);
		if (*end == '/')
			if (!xtables_strtoui(end + 1, &end, &mask, 0, UINT32_MAX))
				xtables_param_act(XTF_BAD_VALUE, "CONNMARK", "--set-xmark/--set-mark", optarg);
		if (*end != '\0')
			xtables_param_act(XTF_BAD_VALUE, "CONNMARK", "--set-xmark/--set-mark", optarg);
		info->mode   = XT_CONNMARK_SET;
		info->ctmark = value;
		info->ctmask = mask;
		if (c == '-')
			info->ctmask |= value;
		*flags |= F_MARK;
		return true;

	case '&': /* --and-mark */
		xtables_param_act(XTF_ONE_ACTION, "CONNMARK", *flags & F_MARK);
		if (!xtables_strtoui(optarg, NULL, &mask, 0, UINT32_MAX))
			xtables_param_act(XTF_BAD_VALUE, "CONNMARK", "--and-mark", optarg);
		info->mode   = XT_CONNMARK_SET;
		info->ctmark = 0;
		info->ctmask = ~mask;
		*flags      |= F_MARK;
		return true;

	case '|': /* --or-mark */
		xtables_param_act(XTF_ONE_ACTION, "CONNMARK", *flags & F_MARK);
		if (!xtables_strtoui(optarg, NULL, &value, 0, UINT32_MAX))
			xtables_param_act(XTF_BAD_VALUE, "CONNMARK", "--or-mark", optarg);
		info->mode   = XT_CONNMARK_SET;
		info->ctmark = value;
		info->ctmask = value;
		*flags      |= F_MARK;
		return true;

	case '^': /* --xor-mark */
		xtables_param_act(XTF_ONE_ACTION, "CONNMARK", *flags & F_MARK);
		if (!xtables_strtoui(optarg, NULL, &value, 0, UINT32_MAX))
			xtables_param_act(XTF_BAD_VALUE, "CONNMARK", "--xor-mark", optarg);
		info->mode   = XT_CONNMARK_SET;
		info->ctmark = value;
		info->ctmask = 0;
		*flags      |= F_MARK;
		return true;

	case 'S': /* --save-mark */
		xtables_param_act(XTF_ONE_ACTION, "CONNMARK", *flags & F_MARK);
		info->mode = XT_CONNMARK_SAVE;
		*flags |= F_MARK | F_SR_MARK;
		return true;

	case 'R': /* --restore-mark */
		xtables_param_act(XTF_ONE_ACTION, "CONNMARK", *flags & F_MARK);
		info->mode = XT_CONNMARK_RESTORE;
		*flags |= F_MARK | F_SR_MARK;
		return true;

	case 'n': /* --nfmask */
		if (!(*flags & F_SR_MARK))
			xtables_error(PARAMETER_PROBLEM, "CONNMARK: --save-mark "
			           "or --restore-mark is required for "
			           "--nfmask");
		if (!xtables_strtoui(optarg, NULL, &value, 0, UINT32_MAX))
			xtables_param_act(XTF_BAD_VALUE, "CONNMARK", "--nfmask", optarg);
		info->nfmask = value;
		return true;

	case 'c': /* --ctmask */
		if (!(*flags & F_SR_MARK))
			xtables_error(PARAMETER_PROBLEM, "CONNMARK: --save-mark "
			           "or --restore-mark is required for "
			           "--ctmask");
		if (!xtables_strtoui(optarg, NULL, &value, 0, UINT32_MAX))
			xtables_param_act(XTF_BAD_VALUE, "CONNMARK", "--ctmask", optarg);
		info->ctmask = value;
		return true;

	case 'm': /* --mask */
		if (!(*flags & F_SR_MARK))
			xtables_error(PARAMETER_PROBLEM, "CONNMARK: --save-mark "
			           "or --restore-mark is required for "
			           "--mask");
		if (!xtables_strtoui(optarg, NULL, &value, 0, UINT32_MAX))
			xtables_param_act(XTF_BAD_VALUE, "CONNMARK", "--mask", optarg);
		info->nfmask = info->ctmask = value;
		return true;
	}

	return false;
}

static void connmark_tg_check(unsigned int flags)
{
	if (!flags)
		xtables_error(PARAMETER_PROBLEM,
		           "CONNMARK target: No operation specified");
}

static void
print_mark(unsigned long mark)
{
	printf("0x%lx", mark);
}

static void
print_mask(const char *text, unsigned long mask)
{
	if (mask != 0xffffffffUL)
		printf("%s0x%lx", text, mask);
}

static void CONNMARK_print(const void *ip,
                           const struct xt_entry_target *target, int numeric)
{
	const struct xt_connmark_target_info *markinfo =
		(const struct xt_connmark_target_info *)target->data;
	switch (markinfo->mode) {
	case XT_CONNMARK_SET:
	    printf("CONNMARK set ");
	    print_mark(markinfo->mark);
	    print_mask("/", markinfo->mask);
	    printf(" ");
	    break;
	case XT_CONNMARK_SAVE:
	    printf("CONNMARK save ");
	    print_mask("mask ", markinfo->mask);
	    printf(" ");
	    break;
	case XT_CONNMARK_RESTORE:
	    printf("CONNMARK restore ");
	    print_mask("mask ", markinfo->mask);
	    break;
	default:
	    printf("ERROR: UNKNOWN CONNMARK MODE ");
	    break;
	}
}

static void
connmark_tg_print(const void *ip, const struct xt_entry_target *target,
                  int numeric)
{
	const struct xt_connmark_tginfo1 *info = (const void *)target->data;

	switch (info->mode) {
	case XT_CONNMARK_SET:
		if (info->ctmark == 0)
			printf("CONNMARK and 0x%x ",
			       (unsigned int)(u_int32_t)~info->ctmask);
		else if (info->ctmark == info->ctmask)
			printf("CONNMARK or 0x%x ", info->ctmark);
		else if (info->ctmask == 0)
			printf("CONNMARK xor 0x%x ", info->ctmark);
		else if (info->ctmask == 0xFFFFFFFFU)
			printf("CONNMARK set 0x%x ", info->ctmark);
		else
			printf("CONNMARK xset 0x%x/0x%x ",
			       info->ctmark, info->ctmask);
		break;
	case XT_CONNMARK_SAVE:
		if (info->nfmask == UINT32_MAX && info->ctmask == UINT32_MAX)
			printf("CONNMARK save ");
		else if (info->nfmask == info->ctmask)
			printf("CONNMARK save mask 0x%x ", info->nfmask);
		else
			printf("CONNMARK save nfmask 0x%x ctmask ~0x%x ",
			       info->nfmask, info->ctmask);
		break;
	case XT_CONNMARK_RESTORE:
		if (info->ctmask == UINT32_MAX && info->nfmask == UINT32_MAX)
			printf("CONNMARK restore ");
		else if (info->ctmask == info->nfmask)
			printf("CONNMARK restore mask 0x%x ", info->ctmask);
		else
			printf("CONNMARK restore ctmask 0x%x nfmask ~0x%x ",
			       info->ctmask, info->nfmask);
		break;

	default:
		printf("ERROR: UNKNOWN CONNMARK MODE");
		break;
	}
}

static void CONNMARK_save(const void *ip, const struct xt_entry_target *target)
{
	const struct xt_connmark_target_info *markinfo =
		(const struct xt_connmark_target_info *)target->data;

	switch (markinfo->mode) {
	case XT_CONNMARK_SET:
	    printf("--set-mark ");
	    print_mark(markinfo->mark);
	    print_mask("/", markinfo->mask);
	    printf(" ");
	    break;
	case XT_CONNMARK_SAVE:
	    printf("--save-mark ");
	    print_mask("--mask ", markinfo->mask);
	    break;
	case XT_CONNMARK_RESTORE:
	    printf("--restore-mark ");
	    print_mask("--mask ", markinfo->mask);
	    break;
	default:
	    printf("ERROR: UNKNOWN CONNMARK MODE ");
	    break;
	}
}

static void CONNMARK_init(struct xt_entry_target *t)
{
	struct xt_connmark_target_info *markinfo
		= (struct xt_connmark_target_info *)t->data;

	markinfo->mask = 0xffffffffUL;
}

static void
connmark_tg_save(const void *ip, const struct xt_entry_target *target)
{
	const struct xt_connmark_tginfo1 *info = (const void *)target->data;

	switch (info->mode) {
	case XT_CONNMARK_SET:
		printf("--set-xmark 0x%x/0x%x ", info->ctmark, info->ctmask);
		break;
	case XT_CONNMARK_SAVE:
		printf("--save-mark --nfmask 0x%x --ctmask 0x%x ",
		       info->nfmask, info->ctmask);
		break;
	case XT_CONNMARK_RESTORE:
		printf("--restore-mark --nfmask 0x%x --ctmask 0x%x ",
		       info->nfmask, info->ctmask);
		break;
	default:
		printf("ERROR: UNKNOWN CONNMARK MODE");
		break;
	}
}

static struct xtables_target connmark_tg_reg[] = {
	{
		.family        = NFPROTO_UNSPEC,
		.name          = "CONNMARK",
		.revision      = 0,
		.version       = XTABLES_VERSION,
		.size          = XT_ALIGN(sizeof(struct xt_connmark_target_info)),
		.userspacesize = XT_ALIGN(sizeof(struct xt_connmark_target_info)),
		.help          = CONNMARK_help,
		.init          = CONNMARK_init,
		.parse         = CONNMARK_parse,
		.final_check   = connmark_tg_check,
		.print         = CONNMARK_print,
		.save          = CONNMARK_save,
		.extra_opts    = CONNMARK_opts,
	},
	{
		.version       = XTABLES_VERSION,
		.name          = "CONNMARK",
		.revision      = 1,
		.family        = NFPROTO_UNSPEC,
		.size          = XT_ALIGN(sizeof(struct xt_connmark_tginfo1)),
		.userspacesize = XT_ALIGN(sizeof(struct xt_connmark_tginfo1)),
		.help          = connmark_tg_help,
		.init          = connmark_tg_init,
		.parse         = connmark_tg_parse,
		.final_check   = connmark_tg_check,
		.print         = connmark_tg_print,
		.save          = connmark_tg_save,
		.extra_opts    = connmark_tg_opts,
	},
};

void libxt_CONNMARK_init(void)
{
	xtables_register_targets(connmark_tg_reg, ARRAY_SIZE(connmark_tg_reg));
}
