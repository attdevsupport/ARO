/*
 *  Copyright 2012 AT&T
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.att.aro.ssl;

import com.att.aro.ssl.crypto_openssl.crypto_cipher_alg;
import com.att.aro.ssl.crypto_openssl.tls_cipher;
import com.att.aro.ssl.crypto_openssl.tls_cipher_type;

public class tls_cipher_data {
	tls_cipher cipher;
	tls_cipher_type type;
	int key_material;
	int expanded_key_material;
	int block_size;
	crypto_cipher_alg alg;
	
	tls_cipher_data() {
		
	}
	
	tls_cipher_data(tls_cipher cipher, tls_cipher_type type, int key_material, int expanded_key_material, int block_size, crypto_cipher_alg alg){
		this.cipher = cipher;
		this.type = type;
		this.key_material = key_material;
		this.expanded_key_material = expanded_key_material;
		this.block_size = block_size;
		this.alg = alg;
	}
}

