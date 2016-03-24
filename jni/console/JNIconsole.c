// compile:
// linux:
// gcc -O2 -shared -fpic -o libJNIconsole.so -I/usr/lib/jvm/java-8-openjdk/include/ -I/usr/lib/jvm/java-8-openjdk/include/linux JNIconsole.c -lreadline
// mac:
// gcc -O2 -shared -fpic -o libJNIconsole.dylib -I/Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/include -I/Library/Java/JavaVirtualMachines/jdk1.8.0_60.jdk/Contents/Home/include/darwin JNIconsole.c -lreadline

#include <jni.h>
#include <stdlib.h>

#if defined (HAVE_CONFIG_H)
#include <config.h>
#endif

#include <stdio.h>
#include <sys/types.h>
#include <string.h>

#ifdef HAVE_STDLIB_H
#  include <stdlib.h>
#else
extern void exit();
#endif

#ifdef READLINE_LIBRARY
#  include "readline.h"
#  include "history.h"
#else
#  include <readline/readline.h>
#  include <readline/history.h>
#endif

extern HIST_ENTRY **history_list ();


JNIEXPORT jbyteArray JNICALL Java_de_hhu_bsinfo_utils_JNIconsole_readline(JNIEnv *p_env, jclass p_class, jstring p_prompt) {
    char *temp, *ptr;
	const char* prompt;
    
    
    temp = (char *)NULL;
    
    prompt = (*p_env)->GetStringUTFChars(p_env, p_prompt, NULL);
    
    temp = readline (prompt);
    
    add_history (temp);
   
    
    int idx,len;
    
    len = strlen(temp);
    
    jbyte buff[len];
    jbyteArray result = (*p_env)->NewByteArray(p_env, len);
    if (result == NULL) {
        free (temp);
        return NULL; /* out of memory error thrown */
    }
    
    
    ptr = temp;
    idx = 0;
    while (*ptr != '\0') {
        buff[idx++] = *ptr;
        ptr++;
    }
    
    free (temp);

    (*p_env)->SetByteArrayRegion(p_env, result, 0, len, buff);
    return result;
}