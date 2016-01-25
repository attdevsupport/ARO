#include "objbase.h"
#include "crypto_common.h"
#pragma once

BEGIN_NAMESPACE(CRYPTO)

#define MP_LOW_MEM

#ifdef MP_LOW_MEM
#define TAB_SIZE 32
#else
#define TAB_SIZE 256
#endif

typedef ULONGLONG mp_word;

#define DIGIT_BIT          28
/* define this to use lower memory usage routines (exptmods mostly) */
#define MP_MASK          ((((mp_digit)1)<<((mp_digit)DIGIT_BIT))-((mp_digit)1))

#define LTM_NO_NEG_EXP
#define BN_MP_INVMOD_C
#define BN_S_MP_EXPTMOD_C
//#define BN_MP_EXPTMOD_FAST_C
#define BN_FAST_S_MP_SQR_C
#define BN_S_MP_SQR_C
#define BN_S_MP_MUL_HIGH_DIGS_C
#define BN_S_MP_MUL_DIGS_C

#define MP_WARRAY               (1 << (sizeof(mp_word) * CHAR_BIT - 2 * DIGIT_BIT + 1))

/* default precision */
#ifndef MP_PREC
#ifndef MP_LOW_MEM
#define MP_PREC                 32     /* default digits of precision */
#else
#define MP_PREC                 8      /* default digits of precision */
#endif   
#endif

typedef int           mp_err;
typedef unsigned long mp_digit;

/* the infamous mp_int structure */
typedef struct  {
	int used, alloc, sign;
	mp_digit *dp;
} mp_int;

#define MP_LT        -1   /* less than */
#define MP_EQ         0   /* equal to */
#define MP_GT         1   /* greater than */

#define MP_ZPOS       0   /* positive integer */
#define MP_NEG        1   /* negative */

#define MP_OKAY       0   /* ok result */
#define MP_MEM        -2  /* out of mem */
#define MP_VAL        -3  /* invalid input */

#define MP_YES        1   /* yes response */
#define MP_NO         0   /* no response */

#define mp_iszero(a) (((a)->used == 0) ? MP_YES : MP_NO)
#define mp_iseven(a) (((a)->used > 0 && (((a)->dp[0] & 1) == 0)) ? MP_YES : MP_NO)
#define mp_isodd(a)  (((a)->used > 0 && (((a)->dp[0] & 1) == 1)) ? MP_YES : MP_NO)
#define s_mp_mul(a, b, c) s_mp_mul_digs(a, b, c, (a)->used + (b)->used + 1)

void mp_clear (mp_int * a);
int mp_init (mp_int * a);
int mp_cmp_d(mp_int * a, mp_digit b);
int mp_read_unsigned_bin (mp_int * a, const unsigned char *b, int c);
void mp_clamp (mp_int * a);
int mp_grow (mp_int * a, int size);
int mp_mul_2d (mp_int * a, int b, mp_int * c);
void mp_zero (mp_int * a);
int mp_lshd (mp_int * a, int b);
int mp_copy (mp_int * a, mp_int * b);
int mp_add (mp_int * a, mp_int * b, mp_int * c);
int mp_cmp (mp_int * a, mp_int * b);
int mp_sub (mp_int * a, mp_int * b, mp_int * c);
int mp_mul (mp_int * a, mp_int * b, mp_int * c);
int mp_mulmod (mp_int * a, mp_int * b, mp_int * c, mp_int * d);
int mp_mod (mp_int * a, mp_int * b, mp_int * c);
int mp_exptmod (mp_int * G, mp_int * X, mp_int * P, mp_int * Y);
int mp_cmp_mag (mp_int * a, mp_int * b);
int mp_div(mp_int * a, mp_int * b, mp_int * c, mp_int * d);
int s_mp_sub (mp_int * a, mp_int * b, mp_int * c);
int s_mp_add (mp_int * a, mp_int * b, mp_int * c);
void mp_set (mp_int * a, mp_digit b);
int mp_abs (mp_int * a, mp_int * b);
void mp_exch (mp_int * a, mp_int * b);
void mp_clear_multi(mp_int *mp, ...);
int mp_unsigned_bin_size (mp_int * a);
int mp_to_unsigned_bin (mp_int * a, unsigned char *b);
int mp_init_multi(mp_int *mp, ...);
int mp_count_bits (mp_int * a);
int mp_div_2d (mp_int * a, int b, mp_int * c, mp_int * d);
int mp_init_copy (mp_int * a, mp_int * b);
int mp_mod_2d (mp_int * a, int b, mp_int * c);
void mp_rshd (mp_int * a, int b);
int mp_exptmod_fast (mp_int * G, mp_int * X, mp_int * P, mp_int * Y, int redmode);
int s_mp_exptmod (mp_int * G, mp_int * X, mp_int * P, mp_int * Y, int redmode);
int mp_sqr (mp_int * a, mp_int * b);
int mp_reduce_setup (mp_int * a, mp_int * b);
int mp_reduce (mp_int * x, mp_int * m, mp_int * mu);
int mp_reduce_2k_setup_l(mp_int *a, mp_int *d);
int mp_reduce_2k_l(mp_int *a, mp_int *n, mp_int *d);
int s_mp_mul_high_digs (mp_int * a, mp_int * b, mp_int * c, int digs);
int s_mp_mul_digs (mp_int * a, mp_int * b, mp_int * c, int digs);
int fast_s_mp_sqr (mp_int * a, mp_int * b);
int s_mp_sqr (mp_int * a, mp_int * b);
int mp_2expt (mp_int * a, int b);
int mp_init_size (mp_int * a, int size);
int fast_s_mp_mul_digs (mp_int * a, mp_int * b, mp_int * c, int digs);

void bn_reverse (unsigned char *s, int len);

END_NAMESPACE
