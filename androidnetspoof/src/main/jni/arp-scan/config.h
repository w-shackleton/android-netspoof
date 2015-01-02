// config.h to support arp-scan on Android

#define DATADIR "../bindata"

/* Define to the appropriate type for 64-bit ints. */
#define ARP_INT64 long int

/* Define to the appropriate snprintf format for 64-bit ints. */
#define ARP_INT64_FORMAT "%ld"

/* Define to 1 if pcap uses BPF */
/* #undef ARP_PCAP_BPF */

/* Define to 1 if pcap uses DLPI */
/* #undef ARP_PCAP_DLPI */

/* Define to the appropriate type for unsigned 64-bit ints. */
#define ARP_UINT64 unsigned long int

/* Define to the appropriate snprintf format for unsigned 64-bit ints. */
#define ARP_UINT64_FORMAT "%lu"

/* Define to the compiler's unused pragma */
#define ATTRIBUTE_UNUSED __attribute__ ((__unused__))

/* Define if SSP C support is enabled. */
#define ENABLE_SSP_CC 1

/* Define to 1 if you have the <arpa/inet.h> header file. */
#define HAVE_ARPA_INET_H 1

/* Define to 1 if you have the <fcntl.h> header file. */
#define HAVE_FCNTL_H 1

/* Define to 1 if you have the `gethostbyname' function. */
#define HAVE_GETHOSTBYNAME 1

/* Define to 1 if you have the <getopt.h> header file. */
#define HAVE_GETOPT_H 1

/* Define to 1 if you have the `gettimeofday' function. */
#define HAVE_GETTIMEOFDAY 1

/* Define to 1 if you have the `inet_ntoa' function. */
#define HAVE_INET_NTOA 1

/* Define to 1 if you have the <inttypes.h> header file. */
#define HAVE_INTTYPES_H 1

/* Define to 1 if `long int' works and is 64 bits. */
#define HAVE_LONG_INT_64 /**/

/* Define to 1 if `long long int' works and is 64 bits. */
/* #undef HAVE_LONG_LONG_INT_64 */

/* Define to 1 if you have the `malloc' function. */
#define HAVE_MALLOC 1

/* Define to 1 if you have the <memory.h> header file. */
#define HAVE_MEMORY_H 1

/* Define to 1 if you have the `memset' function. */
#define HAVE_MEMSET 1

/* Define to 1 if you have the <netdb.h> header file. */
#define HAVE_NETDB_H 1

/* Define to 1 if you have the <netinet/in.h> header file. */
#define HAVE_NETINET_IN_H 1

/* Define to 1 if you have the <netpacket/packet.h> header file. */
#define HAVE_NETPACKET_PACKET_H 1

/* Define to 1 if you have the <net/if_dl.h> header file. */
/* #undef HAVE_NET_IF_DL_H */

/* Define to 1 if you have the <net/if.h> header file. */
#define HAVE_NET_IF_H 1

/* Define to 1 if you have the <net/route.h> header file. */
/* #undef HAVE_NET_ROUTE_H */

/* Define to 1 if you have the <pcap.h> header file. */
#define HAVE_PCAP_H 1

/* Define to 1 if you have posix regex support */
#define HAVE_REGEX_H 1

/* Define to 1 if you have the `select' function. */
#define HAVE_SELECT 1

/* Define to 1 if you have the `socket' function. */
#define HAVE_SOCKET 1

/* Define to 1 if you have the <stdint.h> header file. */
#define HAVE_STDINT_H 1

/* Define to 1 if you have the <stdlib.h> header file. */
#define HAVE_STDLIB_H 1

/* Define to 1 if you have the `strerror' function. */
#define HAVE_STRERROR 1

/* Define to 1 if you have the <strings.h> header file. */
#define HAVE_STRINGS_H 1

/* Define to 1 if you have the <string.h> header file. */
#define HAVE_STRING_H 1

/* Define to 1 if the C library includes the strlcat function */
/* #undef HAVE_STRLCAT */

/* Define to 1 if the C library includes the strlcpy function */
/* #undef HAVE_STRLCPY */

/* Define to 1 if you have the <stropts.h> header file. */
/* #undef HAVE_STROPTS_H */

/* Define to 1 if you have the <sys/bufmod.h> header file. */
/* #undef HAVE_SYS_BUFMOD_H */

/* Define to 1 if you have the <sys/dlpihdr.h> header file. */
/* #undef HAVE_SYS_DLPIHDR_H */

/* Define to 1 if you have the <sys/dlpi.h> header file. */
/* #undef HAVE_SYS_DLPI_H */

/* Define to 1 if you have the <sys/ioctl.h> header file. */
#define HAVE_SYS_IOCTL_H 1

/* Define to 1 if you have the <sys/param.h> header file. */
/* #undef HAVE_SYS_PARAM_H */

/* Define to 1 if you have the <sys/socket.h> header file. */
#define HAVE_SYS_SOCKET_H 1

/* Define to 1 if you have the <sys/sockio.h> header file. */
/* #undef HAVE_SYS_SOCKIO_H */

/* Define to 1 if you have the <sys/stat.h> header file. */
#define HAVE_SYS_STAT_H 1

/* Define to 1 if you have the <sys/sysctl.h> header file. */
/* #undef HAVE_SYS_SYSCTL_H */

/* Define to 1 if you have the <sys/time.h> header file. */
#define HAVE_SYS_TIME_H 1

/* Define to 1 if you have the <sys/types.h> header file. */
#define HAVE_SYS_TYPES_H 1

/* Define to 1 if you have the <unistd.h> header file. */
#define HAVE_UNISTD_H 1

/* Name of package */
#define PACKAGE "arp-scan"

/* Define to the address where bug reports for this package should be sent. */
#define PACKAGE_BUGREPORT "https://github.com/royhills/arp-scan"

/* Define to the full name of this package. */
#define PACKAGE_NAME "arp-scan"

/* Define to the full name and version of this package. */
#define PACKAGE_STRING "arp-scan 1.9.2"

/* Define to the one symbol short name of this package. */
#define PACKAGE_TARNAME "arp-scan"

/* Define to the home page for this package. */
#define PACKAGE_URL ""

/* Define to the version of this package. */
#define PACKAGE_VERSION "1.9.2"

/* Define to 1 if you have the ANSI C header files. */
#define STDC_HEADERS 1

/* Define to 1 if you can safely include both <sys/time.h> and <time.h>. */
#define TIME_WITH_SYS_TIME 1

/* Version number of package */
#define VERSION "1.9.2"

/* Define to empty if `const' does not conform to ANSI C. */
/* #undef const */

/* Define to `unsigned int' if <sys/types.h> does not define. */
/* #undef size_t */

/* Define to required type if we don't have uint16_t */
/* #undef uint16_t */

/* Define to required type if we don't have uint32_t */
/* #undef uint32_t */

/* Define to required type if we don't have uint8_t */
/* #undef uint8_t */
