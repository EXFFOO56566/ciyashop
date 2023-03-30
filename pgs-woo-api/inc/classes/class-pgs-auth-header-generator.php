<?php
/**
 * Pgs_auth_header_generator
 *
 * @package Token generations
 * @author lolkittens
 * @copyright 2018
 * @version $Id$
 * @access public
 */
class Pgs_auth_header_generator {
	private $auth_data;
	private $url;
	private $method;
	private $auth_auth_credentials;
    private $oauth_verifier;
	public function __construct( $auth_data, $url, $method,$oauth_verifier='' ) {
		$this->auth_data   = $auth_data;
		$this->url    = $url;
		$this->method = $method;
        $this->oauth_verifier = $oauth_verifier;
		$this->pgs_woo_api_auth_auth_credentials();
	}
	public function pgs_woo_api_header_data() {
		$header = 'OAuth ';
		$oauth_params = array();
		foreach ( $this->auth_credentials as $key => $value ) {
			$oauth_params[] = "$key=\"" . rawurlencode( $value ) . '"';
		}
		$header .= implode( ', ', $oauth_params );
		return $header;
	}
	private function pgs_woo_api_auth_auth_credentials() {
		$oauth_token = (isset($this->auth_data['oauth_token']))?$this->auth_data['oauth_token']:'';
        $auth_auth_credentials = array(
			'oauth_consumer_key'     => $this->auth_data['oauth_consumer_key'],
            'oauth_token'            => $oauth_token,
			'oauth_signature_method' => 'HMAC-SHA1',
            'oauth_timestamp'        => time(),
            'oauth_nonce'            => wp_generate_password( 12, false, false ),
			'oauth_version'          => '1.0'
		);

        if(isset($this->oauth_verifier) && !empty($this->oauth_verifier) ){
            $authcredentials['oauth_verifier'] = $this->oauth_verifier;
            $auth_auth_credentials = array_merge($auth_auth_credentials,$authcredentials);
            ksort( $auth_auth_credentials );
        } else {
            ksort( $auth_auth_credentials );
        }
		// For some reason, this matters!
		$this->auth_credentials = $auth_auth_credentials;
		$this->pgs_woo_api_create_oauth_signature();
	}
	private function pgs_woo_api_create_oauth_signature() {
		$oauth_consumer_secret = (isset($this->auth_data['oauth_consumer_secret']))?$this->auth_data['oauth_consumer_secret']:'';
        $oauth_token_secret = (isset($this->auth_data['oauth_token_secret']))?$this->auth_data['oauth_token_secret']:'';
        $string_params = array();
		foreach ( $this->auth_credentials as $key => $value ) {
			$string_params[] = "$key=$value";
		}
        $signature = "$this->method&" . rawurlencode( $this->url ) . '&' . rawurlencode( implode( '&', $string_params ) );
		$hash_hmac_key = rawurlencode( $oauth_consumer_secret ) . '&' . rawurlencode( $oauth_token_secret );
		$oauth_signature = base64_encode( hash_hmac( 'sha1', $signature, $hash_hmac_key, true ) );
		$this->auth_credentials['oauth_signature'] = $oauth_signature;

	}
}