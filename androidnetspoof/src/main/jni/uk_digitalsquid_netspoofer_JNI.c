#include "uk_digitalsquid_netspoofer_JNI.h"

#include <sys/stat.h>

#define EXEC_PERM ((S_IRUSR | S_IWUSR | S_IXUSR) | (S_IRGRP | S_IXGRP) | (S_IROTH | S_IXOTH)) /* 00755 */

JNIEXPORT jint JNICALL Java_uk_digitalsquid_netspoofer_JNI_setExecutable(JNIEnv *env, jobject obj, jstring path) {
	jboolean iscopy;
	const char *mpath = (*env)->GetStringUTFChars(env, path, &iscopy);

	chmod(mpath, EXEC_PERM);

	// Clean up
	(*env)->ReleaseStringUTFChars(env, path, mpath);
}
