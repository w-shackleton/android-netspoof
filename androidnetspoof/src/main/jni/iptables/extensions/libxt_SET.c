/* Copyright (C) 2000-2002 Joakim Axelsson <gozem@linux.nu>
 *                         Patrick Schaaf <bof@bof.de>
 *                         Martin Josefsson <gandalf@wlug.westbo.se>
 * Copyright (C) 2003-2010 Jozsef Kadlecsik <kadlec@blackhole.kfki.hu>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 2 as
 * published by the Free Software Foundation.  
 */

/* Shared library add-on to iptables to add IP set mangling target. */
#include <stdbool.h>
#include <stdio.h>
#include <netdb.h>
#include <string.h>
#include <stdlib.h>
#include <getopt.h>
#include <ctype.h>

#include <xtables.h>
#include <linux/netfilter/xt_set.h>
#include "libxt_set.h"

static void
set_target_help(void)
{
	printf("SET target options:\n"
	       " --add-set name flags\n"
	       " --del-set name flags\n"
	       "		add/del src/dst IP/port from/to named sets,\n"
	       "		where flags are the comma separated list of\n"
	       "		'src' and 'dst' specifications.\n");
}

static const struct option set_target_opts[] = {
	{.name = "add-set", .has_arg = true, .val = '1'},
	{.name = "del-set", .has_arg = true, .val = '2'},
	XT_GETOPT_TABLEEND,
};

static void
set_target_check(unsigned int flags)
{
	if (!flags)
		xtables_error(PARAMETER_PROBLEM,
			   "You must specify either `--add-set' or `--del-set'");
}

static void
set_target_init_v0(struct xt_entry_target *target)
{
	struct xt_set_info_target_v0 *info =
		(struct xt_set_info_target_v0 *) target->data;

	info->add_set.index =
	info->del_set.index = IPSET_INVALID_ID;

}

static void
parse_target_v0(char **argv, int invert, unsigned int *flags,
		struct xt_set_info_v0 *info, const char *what)
{
	if (info->u.flags[0])
		xtables_error(PARAMETER_PROBLEM,
			      "--%s can be specified only once", what);

	if (xtables_check_inverse(optarg, &invert, NULL, 0, argv))
		xtables_error(PARAMETER_PROBLEM,
			      "Unexpected `!' after --%s", what);

	if (!argv[optind]
	    || argv[optind][0] == '-' || argv[optind][0] == '!')
		xtables_error(PARAMETER_PROBLEM,
			      "--%s requires two args.", what);

	if (strlen(optarg) > IPSET_MAXNAMELEN - 1)
		xtables_error(PARAMETER_PROBLEM,
			      "setname `%s' too long, max %d characters.",
			      optarg, IPSET_MAXNAMELEN - 1);

	get_set_byname(optarg, (struct xt_set_info *)info);
	parse_dirs_v0(argv[optind], info);
	optind++;
	
	*flags = 1;
}

static int
set_target_parse_v0(int c, char **argv, int invert, unsigned int *flags,
		    const void *entry, struct xt_entry_target **target)
{
	struct xt_set_info_target_v0 *myinfo =
		(struct xt_set_info_target_v0 *) (*target)->data;

	switch (c) {
	case '1':		/* --add-set <set> <flags> */
		parse_target_v0(argv, invert, flags,
				&myinfo->add_set, "add-set");
		break;
	case '2':		/* --del-set <set>[:<flags>] <flags> */
		parse_target_v0(argv, invert, flags,
				&myinfo->del_set, "del-set");
		break;

	default:
		return 0;
	}
	return 1;
}

static void
print_target_v0(const char *prefix, const struct xt_set_info_v0 *info)
{
	int i;
	char setname[IPSET_MAXNAMELEN];

	if (info->index == IPSET_INVALID_ID)
		return;
	get_set_byid(setname, info->index);
	printf("%s %s", prefix, setname);
	for (i = 0; i < IPSET_DIM_MAX; i++) {
		if (!info->u.flags[i])
			break;		
		printf("%s%s",
		       i == 0 ? " " : ",",
		       info->u.flags[i] & IPSET_SRC ? "src" : "dst");
	}
	printf(" ");
}

static void
set_target_print_v0(const void *ip, const struct xt_entry_target *target,
                    int numeric)
{
	const struct xt_set_info_target_v0 *info = (const void *)target->data;

	print_target_v0("add-set", &info->add_set);
	print_target_v0("del-set", &info->del_set);
}

static void
set_target_save_v0(const void *ip, const struct xt_entry_target *target)
{
	const struct xt_set_info_target_v0 *info = (const void *)target->data;

	print_target_v0("--add-set", &info->add_set);
	print_target_v0("--del-set", &info->del_set);
}

static void
set_target_init(struct xt_entry_target *target)
{
	struct xt_set_info_target *info =
		(struct xt_set_info_target *) target->data;

	info->add_set.index =
	info->del_set.index = IPSET_INVALID_ID;

}

static void
parse_target(char **argv, int invert, unsigned int *flags,
	     struct xt_set_info *info, const char *what)
{
	if (info->dim)
		xtables_error(PARAMETER_PROBLEM,
			      "--%s can be specified only once", what);

	if (xtables_check_inverse(optarg, &invert, NULL, 0, argv))
		xtables_error(PARAMETER_PROBLEM,
			      "Unexpected `!' after --%s", what);

	if (!argv[optind]
	    || argv[optind][0] == '-' || argv[optind][0] == '!')
		xtables_error(PARAMETER_PROBLEM,
			      "--%s requires two args.", what);

	if (strlen(optarg) > IPSET_MAXNAMELEN - 1)
		xtables_error(PARAMETER_PROBLEM,
			      "setname `%s' too long, max %d characters.",
			      optarg, IPSET_MAXNAMELEN - 1);

	get_set_byname(optarg, info);
	parse_dirs(argv[optind], info);
	optind++;
	
	*flags = 1;
}

static int
set_target_parse(int c, char **argv, int invert, unsigned int *flags,
		 const void *entry, struct xt_entry_target **target)
{
	struct xt_set_info_target *myinfo =
		(struct xt_set_info_target *) (*target)->data;

	switch (c) {
	case '1':		/* --add-set <set> <flags> */
		parse_target(argv, invert, flags,
			     &myinfo->add_set, "add-set");
		break;
	case '2':		/* --del-set <set>[:<flags>] <flags> */
		parse_target(argv, invert, flags,
			     &myinfo->del_set, "del-set");
		break;

	default:
		return 0;
	}
	return 1;
}

static void
print_target(const char *prefix, const struct xt_set_info *info)
{
	int i;
	char setname[IPSET_MAXNAMELEN];

	if (info->index == IPSET_INVALID_ID)
		return;
	get_set_byid(setname, info->index);
	printf("%s %s", prefix, setname);
	for (i = 1; i <= IPSET_DIM_MAX; i++) {
		printf("%s%s",
		       i == 1 ? " " : ",",
		       info->flags & (1 << i) ? "src" : "dst");
	}
	printf(" ");
}

static void
set_target_print(const void *ip, const struct xt_entry_target *target,
                 int numeric)
{
	const struct xt_set_info_target *info = (const void *)target->data;

	print_target("add-set", &info->add_set);
	print_target("del-set", &info->del_set);
}

static void
set_target_save(const void *ip, const struct xt_entry_target *target)
{
	const struct xt_set_info_target *info = (const void *)target->data;

	print_target("--add-set", &info->add_set);
	print_target("--del-set", &info->del_set);
}

static struct xtables_target set_tg_reg[] = {
	{
		.name		= "SET",
		.revision	= 0,
		.version	= XTABLES_VERSION,
		.family		= NFPROTO_IPV4,
		.size		= XT_ALIGN(sizeof(struct xt_set_info_target_v0)),
		.userspacesize	= XT_ALIGN(sizeof(struct xt_set_info_target_v0)),
		.help		= set_target_help,
		.init		= set_target_init_v0,
		.parse		= set_target_parse_v0,
		.final_check	= set_target_check,
		.print		= set_target_print_v0,
		.save		= set_target_save_v0,
		.extra_opts	= set_target_opts,
	},
	{
		.name		= "SET",
		.revision	= 1,
		.version	= XTABLES_VERSION,
		.family		= NFPROTO_UNSPEC,
		.size		= XT_ALIGN(sizeof(struct xt_set_info_target)),
		.userspacesize	= XT_ALIGN(sizeof(struct xt_set_info_target)),
		.help		= set_target_help,
		.init		= set_target_init,
		.parse		= set_target_parse,
		.final_check	= set_target_check,
		.print		= set_target_print,
		.save		= set_target_save,
		.extra_opts	= set_target_opts,
	},
};

void libxt_SET_init(void)
{
	xtables_register_targets(set_tg_reg, ARRAY_SIZE(set_tg_reg));
}
