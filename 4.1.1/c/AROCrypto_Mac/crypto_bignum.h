#pragma once

BEGIN_NAMESPACE(CRYPTO)

struct bignum;

struct crypto_rsa_key {
	int private_key; /* whether private key is set */
	struct bignum *n; /* modulus (p * q) */
	struct bignum *e; /* public exponent */
	/* The following parameters are available only if private_key is set */
	struct bignum *d; /* private exponent */
	struct bignum *p; /* prime p (factor of n) */
	struct bignum *q; /* prime q (factor of n) */
	struct bignum *dmp1; /* d mod (p - 1); CRT exponent */
	struct bignum *dmq1; /* d mod (q - 1); CRT exponent */
	struct bignum *iqmp; /* 1 / q mod p; CRT coefficient */
};


struct bignum * bignum_init(void);
void bignum_deinit(struct bignum *n);
int bignum_set_unsigned_bin(struct bignum *n, const u8 *buf, size_t len);
int bignum_cmp_d(const struct bignum *a, unsigned long b);
int bignum_exptmod(const struct bignum *a, const struct bignum *b,
				   const struct bignum *c, struct bignum *d);
int bignum_mulmod(const struct bignum *a, const struct bignum *b, 
				  const struct bignum *c, struct bignum *d);
int bignum_mul(const struct bignum *a, const struct bignum *b, struct bignum *c);
int bignum_sub(const struct bignum *a, const struct bignum *b, struct bignum *c);
int bignum_add(const struct bignum *a, const struct bignum *b, struct bignum *c);
int bignum_cmp(const struct bignum *a, const struct bignum *b);
size_t bignum_get_unsigned_bin_len(struct bignum *n);
int bignum_get_unsigned_bin(const struct bignum *n, u8 *buf, size_t *len);

END_NAMESPACE
