<?php
require_once( PGS_API_PATH . 'inc/classes/class-pgs-auth-header-generator.php' );
/**
 * Test connection to API with OAuth1 authentication headers.
 * Shortcode [pgs_woo_api_check_oauth_connection]
 */
add_shortcode( 'pgs_woo_api_check_oauth_connection', 'pgs_woo_api_call_test_api' );
function pgs_woo_api_call_test_api($atts) {


    $atts = shortcode_atts( array(
		'endpoint' => 'test_api',
	), $atts, 'pgs_woo_api_check_oauth_connection' );

	$endpoint = $atts['endpoint'];

    $url = site_url('wp-json/pgs-woo-api/v1/'.$endpoint);
	$method = 'POST';
    $pgs_woo_api = get_option('app_auth');
    $client_key='';$client_secret='';$token='';$token_secret='';
    if(isset($pgs_woo_api['pgs_auth']) && !empty($pgs_woo_api['pgs_auth'])){
        $pgs_auth = $pgs_woo_api;
        $client_key = (isset($pgs_auth['pgs_auth']['client_key']))?$pgs_auth['pgs_auth']['client_key']:'';
        $client_secret = (isset($pgs_auth['pgs_auth']['client_secret']))?$pgs_auth['pgs_auth']['client_secret']:'';
        $token = (isset($pgs_auth['pgs_auth']['token']))?$pgs_auth['pgs_auth']['token']:'';
        $token_secret = (isset($pgs_auth['pgs_auth']['token_secret']))?$pgs_auth['pgs_auth']['token_secret']:'';
    }
    if( $client_key != '' && $client_secret != '' && $token != '' && $token_secret != '' ){
        $auth_data = array(
    		'oauth_consumer_key'    => $client_key,
    		'oauth_consumer_secret' => $client_secret,
    		'oauth_token'           => $token,
    		'oauth_token_secret'    => $token_secret,
    	);

    	$oauth = new Pgs_auth_header_generator( $auth_data, $url, $method );
    	$header_data = $oauth->pgs_woo_api_header_data();

        $header = array(
            'Content-Type: application/json',
            'Authorization:'.$header_data
        );


        $ch = curl_init($url);
        $posts = array();
        $posts = json_encode($posts);
        $responce = '';$error_msg='';
        curl_setopt($ch, CURLOPT_TIMEOUT, 1000);
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
        curl_setopt($ch, CURLOPT_POSTFIELDS, $posts);
        curl_setopt($ch, CURLOPT_HTTPHEADER, $header);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);
		curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, 0);
        $responce = curl_exec($ch);
        if (curl_error($ch)) {
            $error_msg = curl_error($ch);
        }
        curl_close($ch);
        return json_encode($responce.$error_msg);
    } else {
        $msg_array = array(
            "client_key" => $client_key,
            "client_secret" => $client_secret,
            "token" => $token,
            "token_secret" => $token_secret
        );
        $msg = array();
        foreach($msg_array as $key => $val){
            if(empty($val)){
                $msg[] = ucwords(str_replace("_", " ", $key));
            }
        }
        $error = implode(",",$msg);
        return json_encode( esc_html__( "Missing oauth credentials $error in API settings page -> API Keys tab.",'pgs-woo-api' ));
    }
}