<?php
if ( ! function_exists( 'pgs_woo_api_get_woowallet_topup_page' ) ) {

    /**
     * get woo wallet topup page
     * @return WP_Post object
     */
    function pgs_woo_api_get_woowallet_topup_page() {
        PGS_Woo_API_WooWallet::cteate_woowallet_topup_page_if_not_exist();
		$woowallet_topup_page_id = get_option( '_pgs_woo_api_woowallet_topup_page' );
		$woowallet_topup_page_id = apply_filters( 'pgs_woo_api_woowallet_topup_page_id', $woowallet_topup_page_id );
		$woowallet_topup_page = get_post( $woowallet_topup_page_id );
		return $woowallet_topup_page;
    }
}

/**
 * Set custom template for WooWallet Topup page.
 */
add_filter( 'page_template', 'pgs_woo_api_woowallet_topup_page' );
function pgs_woo_api_woowallet_topup_page( $page_template ){

    $woowallet_topup_page = pgs_woo_api_get_woowallet_topup_page();

	global $post;

	if( $woowallet_topup_page && $woowallet_topup_page->ID == $post->ID ) {
		/*
		// WPML Integration
		if( $woowallet_topup_page ) {
			$lang = '';
			$is_wpml_active = pgs_woo_api_is_wpml_active();
			if($is_wpml_active){
				if( defined( 'ICL_LANGUAGE_CODE' ) && function_exists( 'icl_object_id' ) ) {
					$input = file_get_contents("php://input");
					$request = json_decode($input,true);
					if(isset($request['lang_is']) && !empty($request['lang_is'])){
						$lang = $request['lang_is'];
					}
					$checkout_page = icl_object_id( $checkout_page, 'post', true, $lang );
				}
			}
			$checkoutpage = get_post($checkout_page);
			if(isset($checkoutpage)){
				$appcheckout = $checkoutpage->post_name;
			}
		}
		*/
		$page_template = PGS_API_PATH . 'template/woowallet_topup_webview.php';
	}
    return $page_template;
}

add_filter( 'woocommerce_get_checkout_url', 'pgs_woo_api_woowallet_checkout_url', 999999 );
function pgs_woo_api_woowallet_checkout_url( $checkout_url ) {
	if ( isset( $_POST['woo_wallet_balance_to_add'] ) && ! empty( $_POST['woo_wallet_balance_to_add'] ) ) {
		$woowallet_topup_page = pgs_woo_api_get_woowallet_topup_page();
		if ( $woowallet_topup_page && is_a( $woowallet_topup_page, 'WP_Post' ) && isset( $_POST['woo_add_to_wallet_source'] ) ) {
			$woowallet_topup_page_id = $woowallet_topup_page->ID;
			$woo_add_to_wallet_page  = (int) sanitize_text_field( $_POST['woo_add_to_wallet_source'] );

			if ( $woowallet_topup_page_id == $woo_add_to_wallet_page ){
				$checkout_url = pgs_woo_api_get_app_checkout_page();
			}
		}
	}

	return $checkout_url;
}

/**
 *  This function will remove all of the WooCommerce standard gateways from the
 *  WooCommerce > Settings > Checkout dashboard.
 *  @see current default gateways https://github.com/woocommerce/woocommerce/blob/master/includes/class-wc-payment-gateways.php#L77-L85
 */
function pgs_woo_api_wc_remove_default_payment_gateways( $load_gateways ) {

	// Bail early, if in Admin Panel
	if ( is_admin() ) {
		return $load_gateways;
	}

	// Bail early, if cart does not contain rechargeable product.
	$product = get_wallet_rechargeable_product();
	if ( ! is_wallet_rechargeable_cart() ) {
		return $load_gateways;
	}

	if ( defined( 'PGS_WOO_API__TERAWALLET__ENABLE_DEFAULT_PAYMENT_GATEWAYS' ) && PGS_WOO_API__TERAWALLET__ENABLE_DEFAULT_PAYMENT_GATEWAYS ) {
		return $load_gateways;
	}

	$remove_gateways = array(
		'WC_Gateway_BACS',
		'WC_Gateway_Cheque',
		'WC_Gateway_COD',
		// 'WC_Gateway_Paypal'
	);

	foreach ( $load_gateways as $gateway_index => $gateway_data ) {
		$gateway_name = $gateway_data;

		if ( is_object( $gateway_data ) ) {
			$gateway_name = get_class( $gateway_data );
		}

		if ( in_array( $gateway_name, $remove_gateways, true ) ) {
			unset( $load_gateways[ $gateway_index ] );
		}
	}

	return $load_gateways;
}
add_filter( 'woocommerce_payment_gateways', 'pgs_woo_api_wc_remove_default_payment_gateways', 20, 1 );
/**
 *  This function add notice in Tera Wallet setting for disabled default WC payment gateways.
 *  @see https://plugins.trac.wordpress.org/browser/woo-wallet/trunk/includes/class-woo-wallet-settings.php#L246
 */
function woo_wallet_update_settings_filds( $settings_fields ) {

	$wc_payment_allowed_gateways_notice = '<span class="dashicons dashicons-warning" style="color:#ff0000;"></span> ';
	$wc_payment_allowed_gateways_notice .= __( 'For security reasons, default WooCommerce payment methods, the list provided below, are disabled. So, the below payment methods will not be displayed even if they are selected.', 'pgs-woo-api' );
	$wc_payment_allowed_gateways_notice .= '<ol>';
	$wc_payment_allowed_gateways_notice .= '<li>Direct Bank Transfer</li>';
	$wc_payment_allowed_gateways_notice .= '<li>Check Payments</li>';
	$wc_payment_allowed_gateways_notice .= '<li>Cash on Delivery</li>';
	$wc_payment_allowed_gateways_notice .= '</ol>';
	$wc_payment_allowed_gateways_notice .= __( 'If you want to enable these payment methods, you can do it by adding the below-provided code to <code>wp-config.php</code>.', 'pgs-woo-api' );
	$wc_payment_allowed_gateways_notice .= '<pre style="background-color:#eaeaea;padding:2px 5px;">define( \'PGS_WOO_API__TERAWALLET__ENABLE_DEFAULT_PAYMENT_GATEWAYS\', true );</pre>';

	$settings_fields['_wallet_settings_general'] = array_merge( $settings_fields['_wallet_settings_general'], array(
		array(
			'name'    => 'wc_payment_allowed_gateways_notice',
			'label'   => esc_html__( 'Payment Gateway Notice', 'pgs-woo-api' ),
			'desc'    => wp_kses( $wc_payment_allowed_gateways_notice, array(
				'span' => array(
					'class' => true,
					'style' => true,
				),
				'code' => array(),
				'ol'   => array(),
				'li'   => array(),
				'pre'  => array(
					'style' => true,
				),
			) ),
			'type'    => 'html',
			'default' => '',
		)
	) );

	return $settings_fields;
}
add_filter( 'woo_wallet_settings_filds', 'woo_wallet_update_settings_filds' );
