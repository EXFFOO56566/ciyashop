<?php
/**
 * update default Post API /wp-json/wp/v2/posts responce for add featured image src.
 */
add_filter( "rest_prepare_post","pgs_woo_api_rest_prepare_post",10,3  );
function pgs_woo_api_rest_prepare_post($response, $post, $request){
    $image_src = array();
    $image_size_array = array('thumbnail', 'medium', 'medium_large', 'large');
    foreach($image_size_array as $size){
        $image_src[$size] = get_the_post_thumbnail_url($post->ID,$size);
    }
    $response->data['featured_image_src'] = $image_src;
    return $response;
}

// ajax hook check validate postalcode
add_action('wp_ajax_pgs_woo_api_check_postalcode', 'pgs_woo_api_check_postalcode');
add_action('wp_ajax_nopriv_pgs_woo_api_check_postalcode', 'pgs_woo_api_check_postalcode');
// check postcode valid
function pgs_woo_api_check_postalcode(){
	$billing_country_code = $_POST['billing_country_code'];
    $billing_postcode = $_POST['billing_postcode'];
    $shipping_country_code = $_POST['shipping_country_code'];
	$shipping_postcode = $_POST['shipping_postcode'];
    $return['billing_postcode'] = '';$return['shipping_postcode'] = '';
    if(!empty($billing_country_code)){
        $return['billing_postcode'] = pgs_woo_api_is_postcode($billing_postcode,$billing_country_code);
    }
    if(!empty($shipping_country_code)){
        $return['shipping_postcode'] = pgs_woo_api_is_postcode($shipping_postcode,$shipping_country_code);
    }
    echo wp_json_encode($return);
	exit;
}

function pgs_woo_api_is_postcode($postcode,$country_code){
    if ( ! WC_Validation::is_postcode( $postcode, $country_code ) ) {
		$return = "No";
	} else {
		$return = "Yes";
	}
    return $return;
}


/**
 * Check is web view muktisteps checkout is enable.
 */
function is_webview_multistep_checkout(){
    $is_pgs_multisteps = is_pgs_multisteps_checkout_active();
    if($is_pgs_multisteps){
        $pgs_woo_api_checkout_multistep = "disable";
        $pgs_woo_api_checkout_multistep = get_option('pgs_woo_api_checkout_multistep');
        if(!empty($pgs_woo_api_checkout_multistep) && $pgs_woo_api_checkout_multistep == "enable"){
            return true;
        }
    }
    return false;
}

/**
 * Add custom image size for app thaumbail image for produts etc
 */
add_action( 'after_setup_theme', 'pgs_woo_api_app_custom_images' );
function pgs_woo_api_app_custom_images(){
    add_image_size( 'app_thumbnail', 260, 290, true );
}

/**
 * Content Wrappers.
 *
 * @see pgs_woo_api_app_checkout_output_content_wrapper_start()
 * @see pgs_woo_api_app_checkout_output_content_wrapper_end()
 */
add_action( 'pgs_woo_api_app_checkout_content_wrapper_start', 'pgs_woo_api_app_checkout_output_content_wrapper_start', 10 );
add_action( 'pgs_woo_api_app_checkout_content_wrapper_end', 'pgs_woo_api_app_checkout_output_content_wrapper_end', 10 );

function pgs_woo_api_app_checkout_output_content_wrapper_start(){
    $html  = '<div id="page" class="hfeed site">';
    $html .= '<div id="content" class="site-content">';
    $html .= '<div class="container">';
    echo $html;
}

function pgs_woo_api_app_checkout_output_content_wrapper_end(){
    $html   = '</div>';
    $html  .= '</div>';
    $html  .= '</div><!-- .site -->';
    echo $html;
}

/**
* Create table for push_notification.
*/
function pgs_woo_api_add_push_notification_tabel_schema(){
    global $wpdb;
    $push_table = $wpdb->prefix.'pgs_woo_api_notifications';
    if($wpdb->get_var("SHOW TABLES LIKE '$push_table'") != $push_table) {
         //table not in database. Create new table
        $charset_collate = $wpdb->get_charset_collate();
         $sql = "CREATE TABLE $push_table (
                id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                user_id INT NOT NULL,
                device_token text NOT NULL,
                device_type tinyint NOT NULL,
                status tinyint(1) NOT NULL DEFAULT '1'
            ) $charset_collate ENGINE = MYISAM";
         require_once( ABSPATH . 'wp-admin/includes/upgrade.php' );
         dbDelta( $sql );
    }

    $push_meta_table = $wpdb->prefix.'pgs_woo_api_notifications_meta';
    if($wpdb->get_var("SHOW TABLES LIKE '$push_meta_table'") != $push_meta_table) {
         //table not in database. Create new table
        $charset_collate = $wpdb->get_charset_collate();
         $sql = "CREATE TABLE $push_meta_table (
                id bigint(20) NOT NULL AUTO_INCREMENT PRIMARY KEY,
                msg text NOT NULL,
                custom_msg text NOT NULL,
                not_code tinyint NOT NULL,
                created datetime NULL
            ) $charset_collate ENGINE = InnoDB";
         require_once( ABSPATH . 'wp-admin/includes/upgrade.php' );
         dbDelta( $sql );
    }

    $push_relationships = $wpdb->prefix.'pgs_woo_api_notifications_relationships';
    if($wpdb->get_var("SHOW TABLES LIKE '$push_relationships'") != $push_relationships) {
         //table not in database. Create new table
        $charset_collate = $wpdb->get_charset_collate();
        $push_relationships_ibfk_2 = $push_relationships.'_ibfk_2';

        $sql = "CREATE TABLE $push_relationships (
        id bigint(20) unsigned NOT NULL AUTO_INCREMENT,
        not_id int(10) unsigned NOT NULL,
        user_id int(10) unsigned NOT NULL,
        push_meta_id bigint(20) NOT NULL,
        PRIMARY KEY (id),
        KEY push_meta_id (push_meta_id),
        CONSTRAINT $push_relationships_ibfk_2 FOREIGN KEY (push_meta_id) REFERENCES $push_meta_table (id) ON DELETE CASCADE
        ) $charset_collate ENGINE = InnoDB";
         require_once( ABSPATH . 'wp-admin/includes/upgrade.php' );
         dbDelta( $sql );
    }

    $scratch_coupons = $wpdb->prefix.'pgs_woo_api_scratch_coupons';
    if($wpdb->get_var("SHOW TABLES LIKE '$scratch_coupons'") != $scratch_coupons) {
         //table not in database. Create new table
        $charset_collate = $wpdb->get_charset_collate();
        $sql = "CREATE TABLE $scratch_coupons (
        id bigint(20) unsigned NOT NULL AUTO_INCREMENT,
        coupon_id int(10) unsigned NOT NULL,
        user_id int(10) unsigned NOT NULL,
        device_token text NOT NULL,
        is_coupon_scratched varchar(5) NOT NULL,
        PRIMARY KEY (id) )
        $charset_collate ENGINE = InnoDB";
        require_once( ABSPATH . 'wp-admin/includes/upgrade.php' );
        dbDelta( $sql );
    }


    /**
    * Add custom table when plugin active for geofencing.
    */
    $table_name = $wpdb->prefix.'pgs_woo_api_geo_fencing';
    if($wpdb->get_var("SHOW TABLES LIKE '$table_name'") != $table_name) {
         //table not in database. Create new table
        $charset_collate = $wpdb->get_charset_collate();
         $sql = "CREATE TABLE $table_name (
                id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                post_id INT NOT NULL,
                radius DOUBLE NOT NULL,
                lat DOUBLE NOT NULL,
                lng DOUBLE NOT NULL,
                zoom int(4) NOT NULL
            ) $charset_collate ENGINE = MYISAM";
         require_once( ABSPATH . 'wp-admin/includes/upgrade.php' );
         dbDelta( $sql );
    }
}

/**
* Add / Update data
*/
function pgs_woo_api_add_push_notification_data($device_token,$device_type,$user_id,$status=null){
    global $wpdb;
    $table_name = $wpdb->prefix . "pgs_woo_api_notifications";
    $qur = "SELECT * FROM $table_name WHERE device_token = '$device_token'";
    $results = $wpdb->get_results( $qur, OBJECT );

    if(isset($device_type) && $device_type == "android"){
        $device_type = 2;
    }

    $data = array(
        'user_id' => $user_id,
        'device_token' => $device_token,
        'device_type' => $device_type,
    );
    $formate = array(
            '%d',
            '%s',
            '%d'
    );
    if($status != null){
        $data['status'] = $status;
        $formate[] = '%d';
    }

    if(!empty($results)){
        $wpdb->update($table_name, $data, array('device_token' => $device_token),$formate,array('%s'));
    } else {
        $wpdb->insert( $table_name,$data,$formate );
    }
}

function pgs_woo_api_push_status(){
    $pushstatus = get_option('pgs_push_status');
    $push_status = (isset($pushstatus) && !empty($pushstatus))?$pushstatus:'enable';
    if($push_status == 'enable'){
        return true;
    } else {
        return false;
    }
}

/**
* Get All data
*/
function pgs_woo_api_get_push_notification_data($user_id){
    global $wpdb;

    $table_name = $wpdb->prefix . "pgs_woo_api_notifications";
    $where = '';
    if($user_id > 0){
        $where = ' WHERE user_id = '.$user_id;
    }
    $qur = "SELECT * FROM $table_name".$where;
    $results = $wpdb->get_results( $qur, OBJECT );
    return $results;
}

/**
* Get notification data
*/
function pgs_woo_api_get_notification_data($not_key){

    $key = (string)$not_key;
    $pgs_not_code = get_option('pgs_not_code');
    $title = '';$message='';
    if(isset($pgs_not_code) && !empty($pgs_not_code)){
        $title = (isset($pgs_not_code[$key]['title']))?$pgs_not_code[$key]['title']:'';
        $message = (isset($pgs_not_code[$key]['message']))?$pgs_not_code[$key]['message']:'';
    }
    return array(
        'title' => $title,
        'message' => $message
    );
}

/**
 * Check Whishlist plugin is activated
 */
function pgs_woo_api_is_wishlist_active(){
    $plugin = 'yith-woocommerce-wishlist/init.php';
    if (is_plugin_active($plugin)) {
        return true;
    } else {
        return false;
    }
}

/**
 * Check YITH WooCommerce Featured Video plugin is activated
 */
function pgs_woo_api_is_yith_featured_video_active(){
    $plugin = 'yith-woocommerce-featured-video/init.php';
    if (is_plugin_active($plugin)) {
        return true;
    } else {
        return false;
    }
}

/**
 * Check Currency Switcher plugin is activated
 */
function pgs_woo_api_is_currency_switcher_active(){
	$plugin = 'woocommerce-currency-switcher/index.php';
	global $WOOCS;
	if ( is_plugin_active( $plugin ) && $WOOCS ) {
        return true;
    } else {
        return false;
    }
}

/**
 * Check Order Tracking plugin is activated
 */
function pgs_woo_api_is_order_tracking_active(){
    $plugin     = 'aftership-woocommerce-tracking/aftership.php';
    $plugin_new = 'aftership-woocommerce-tracking/aftership-woocommerce-tracking.php';

    if ( is_plugin_active($plugin) || is_plugin_active( $plugin_new ) ) {
        return true;
    } else {
        return false;
    }
}


/**
 * Check guest checkout is activated
 */
function pgs_woo_api_is_guest_checkout(){
    if ("yes" === get_option( 'woocommerce_enable_guest_checkout' )) {
        return true;
    } else {
        return false;
    }
}


/**
 * Check Order Tracking plugin is activated
 */
function pgs_woo_api_is_reward_points_active(){
    $plugin = 'woocommerce-points-and-rewards/woocommerce-points-and-rewards.php';
    if (is_plugin_active($plugin)) {
        return true;
    } else {
        return false;
    }
}

/**
 * Check dokan plugin is activated
 */
function pgs_woo_api_is_dokan_active(){
    $plugin = 'dokan-lite/dokan.php';
    if (is_plugin_active($plugin)) {
        return true;
    } else {
        return false;
    }
}

/**
 * Check dokan pro plugin is activated
 */
function pgs_woo_api_is_dokan_pro_active(){
    $plugin = 'dokan-pro/dokan-pro.php';
    if (is_plugin_active($plugin)) {
        return true;
    } else {
        return false;
    }
}

/**
 * Check WC Marketplace plugin is activated
 */
function pgs_woo_api_is_wc_marketplace_active(){
    $plugin = 'dc-woocommerce-multi-vendor/dc_product_vendor.php';
    if (is_plugin_active($plugin)) {
        return true;
    } else {
        return false;
    }
}

/**
 * Check whether WCFM plugin is active
 */
function pgs_woo_api_is_wcfm_active(){
    $plugin = 'wc-multivendor-marketplace/wc-multivendor-marketplace.php';
    if (is_plugin_active($plugin)) {
        return true;
    } else {
        return false;
    }
}

/**
 * Check PGS Multisteps Checkout plugin is activated
 */
function is_pgs_multisteps_checkout_active(){
    $return = ( class_exists('PGS_WOO_API_MultiSteps_Checkout') ? true : false );
    return $return;
}

/**
 * Check WPML plugin is activated
 */
function pgs_woo_api_is_wpml_active(){
    $return = ( class_exists('SitePress') ? true : false );
    if($return){
        global $sitepress;
        $language_negotiation_type = $sitepress->get_setting( 'language_negotiation_type' );
        if($language_negotiation_type != 1){
            $return = false;
        }
    }
    return $return;
}

function pgs_woo_api_is_vendor_plugin_active(){

    $is_vendor = pgs_woo_api_is_dokan_active();
    $is_vendor_2 = pgs_woo_api_is_wc_marketplace_active();
    $is_vendor_3 = pgs_woo_api_is_wcfm_active();
    if(!$is_vendor && !$is_vendor_2 && !$is_vendor_3) {
        $data = array(
            'vendor_active' => false,
            'vendor_count' => 0,
        );
        return $data;
    }

    $cnt = 0;
    if($is_vendor) {
        $cnt += 1;
        $data = array(
            'vendor_active' => true,
            'vendor_count' => 1,
            'vendor_for' => 'dokan',
        );
    }
    if ($is_vendor_2) {
        $cnt += 1;
        $data = array(
            'vendor_active' => true,
            'vendor_count' => 1,
            'vendor_for' => 'wc_marketplace',
        );
    }
	if ($is_vendor_3) {
        $cnt += 1;
        $data = array(
            'vendor_active' => true,
            'vendor_count' => 1,
            'vendor_for' => 'wcfm',
        );
    }
    if($cnt > 1){
        $activevendor = get_option('pgs_active_vendor');
        $active_vendor = (isset($activevendor) && !empty($activevendor)) ? $activevendor : 'dokan';
        $data = array(
            'vendor_active' => true,
            'vendor_count' => $cnt,
            'vendor_for' => $active_vendor,
        );
    }
    return $data;
}


/**
 * Remove include tax html in price html
 */
function pgs_woo_api_hook_remove_tax_in_price_html(){
    add_filter( 'woocommerce_get_price_html', 'pgs_remove_tax_in_price_html', 100, 2 );
}
function pgs_remove_tax_in_price_html( $price, $product ){
    $html = preg_replace('# <small class="woocommerce-price-suffix"(.*?)</small>#', '', $price);
    return $html;
}

/**
* Update currency rale if currency switcher plugin is active
* Call this filter for default woocommerce price on variations product.
*/
add_filter( "woocommerce_rest_prepare_product_variation_object", 'pgs_woo_api_woocommerce_rest_prepare_product_object',10,3 );
function pgs_woo_api_woocommerce_rest_prepare_product_object( $response, $object, $request ){
    $is_currency_switcher_active = pgs_woo_api_is_currency_switcher_active();
    $wc_tax_enabled = wc_tax_enabled();
    $tax_status =  'none';
    $tax_class = '';
    if($wc_tax_enabled){
        $tax_price = wc_get_price_to_display( $object );	//tax
        $price_including_tax = wc_get_price_including_tax( $object );
        $price_excluding_tax = wc_get_price_excluding_tax( $object );
        $tax_status =  $object->get_tax_status();
        $tax_class = $object->get_tax_class();
    }
    if($is_currency_switcher_active){
        $get_price = pgs_woo_api_update_currency_rate_for_default_api($response->data['price']);
        $regular_price = pgs_woo_api_update_currency_rate_for_default_api($response->data['regular_price']);
        $sale_price = pgs_woo_api_update_currency_rate_for_default_api($response->data['sale_price']);

        $response->data['price'] = $get_price;
        $response->data['regular_price'] = $regular_price;
        $response->data['sale_price'] = $sale_price;
        if($wc_tax_enabled){
            $tax_price = pgs_woo_api_update_currency_rate_for_default_api($tax_price);
            $price_including_tax = pgs_woo_api_update_currency_rate_for_default_api($price_including_tax);
            $price_excluding_tax = pgs_woo_api_update_currency_rate_for_default_api($price_excluding_tax);
        }
    }
    $response->data['tax_price'] = (isset($tax_price))?$tax_price:'';
    $response->data['price_including_tax'] = (isset($price_including_tax))?$price_including_tax:'';
    $response->data['price_excluding_tax'] = (isset($price_excluding_tax))?$price_excluding_tax:'';
    $response->data['tax_status'] =  $tax_status;
    $response->data['tax_class'] = $tax_class;
    return apply_filters( "pgs_woo_api_woocommerce_rest_prepare_product_object", $response, $object, $request );
}


function pgs_woo_api_update_currency_rate_for_default_api($price){

    if(!empty($price)){
        global $WOOCS;
		if( $WOOCS && ! get_option('woocs_is_multiple_allowed', 0) ) {
			$currencies = $WOOCS->get_currencies();
			if ($WOOCS->current_currency != $WOOCS->default_currency) {
				//Convertion of currency
				if (in_array($WOOCS->current_currency, $WOOCS->no_cents)/* OR $currencies[$this->current_currency]['hide_cents'] == 1 */) {
					$precision = 0;
				} else {
					if ($WOOCS->current_currency != $WOOCS->default_currency) {
						$precision = $WOOCS->get_currency_price_num_decimals($WOOCS->current_currency, $WOOCS->price_num_decimals);
					} else {
						$precision = $WOOCS->get_currency_price_num_decimals($WOOCS->default_currency, $WOOCS->price_num_decimals);
					}
				}
				if (isset($currencies[$WOOCS->current_currency]) AND $currencies[$WOOCS->current_currency] != NULL) {
					$price = number_format(floatval((float) $price * (float) $currencies[$WOOCS->current_currency]['rate']), $precision, $WOOCS->decimal_sep, '');
				} else {
					$price = number_format(floatval((float) $price * (float) $currencies[$WOOCS->default_currency]['rate']), $precision, $WOOCS->decimal_sep, '');
				}
			}
        }
    }
    return $price;
}


/**
 * Get Woocommerce price option data for price formating
 */
function get_woo_price_formate_option_array(){
    $currency_pos = get_option( 'woocommerce_currency_pos' );
    $arr = array(
        'decimal_separator'  => wc_get_price_decimal_separator(),
        'thousand_separator' => wc_get_price_thousand_separator(),
        'decimals'           => wc_get_price_decimals(),
        'currency_pos'       => $currency_pos,
        'currency_symbol'    => get_woocommerce_currency_symbol(),
        'currency_code' => get_woocommerce_currency()
    );
    return $arr;
}

// WooCoommece Rest API for set price html in
add_filter('woocommerce_rest_prepare_product_variation_object','pgs_api_woocommerce_rest_prepare_product_variation_object',10,3);
function pgs_api_woocommerce_rest_prepare_product_variation_object($response, $object, $request){
    $html = $object->get_price_html();
    $price_html = preg_replace('# <small class="woocommerce-price-suffix"(.*?)</small>#', '', $html);
    $response->data['price_html'] = $price_html;
    return $response;
}

function pgs_woo_script_style_admin() {

    wp_register_style( 'pgs-woo-api-geofance-css', PGS_API_URL.'css/geofance.css' );
    wp_register_style( 'jquery-ui', PGS_API_URL.'css/jquery-ui.min.css' );
    wp_register_style( 'pgs-woo-api-support', PGS_API_URL.'css/pgs-woo-api-support.css' );
    wp_register_style( 'jquery-confirm-bootstrap' , PGS_API_URL.'css/jquery-confirm/jquery-confirm-bootstrap.css' );
    wp_register_style( 'jquery-confirm', PGS_API_URL.'css/jquery-confirm/jquery-confirm.css' );
    wp_register_style( 'pgs-woo-api-css', PGS_API_URL.'css/pgs-woo-api.css' );

    wp_register_script('jquery-repeater-min', PGS_API_URL.'js/jquery.repeater.min.js', array(), false, false );
    wp_register_script( 'jquery-confirm', PGS_API_URL.'js/jquery-confirm/jquery-confirm.js', array('jquery'), false, false );
    wp_register_script( 'pgs-ace-editor-js', PGS_API_URL.'js/ace_editor/ace.js', array('jquery'), false, false );
    wp_register_script('pgs-woo-api-js', PGS_API_URL.'js/pgs-woo-api.js', array('jquery-ui-core','jquery-ui-tabs','jquery-ui-datepicker','jquery-ui-sortable','media-upload','thickbox','wp-color-picker','jquery-repeater-min'), false, false );
    wp_register_script('pgs-woo-api-confirm-custom-js', PGS_API_URL.'js/confirm-custom.js', array('jquery-confirm','wp-util'), false, false );

    wp_localize_script( 'pgs-woo-api-js', 'pgs_woo_api', array(
    	'plugin_url' => plugins_url(),
    	'pgs_api_url' => PGS_API_URL,
        'delete_msg' => esc_html__("Are you sure you want to delete this element?",'pgs-woo-api'),
        'choose_image' => esc_html__("Choose Image",'pgs-woo-api'),
        'add_image' => esc_html__( 'Add Image','pgs-woo-api')
    ) );
    $google_key = pgs_woo_api_get_google_map_api_key();
    wp_register_script( 'pgs-woo-api-google-maps-apis' , 'https://maps.googleapis.com/maps/api/js?key='.$google_key.'&libraries=drawing,places&callback=geoFenc', array(), false, true );
    wp_register_script( 'pgs-woo-api-geofance' , PGS_API_URL.'js/geofance.js', array(),false,true );

    if( ( isset( $_GET['page'] ) && $_GET['page'] == 'pgs-woo-api-settings' ) ||
        ( isset( $_GET['page'] ) && $_GET['page'] == 'pgs-woo-api-token-settings' ) ||
        ( isset( $_GET['page'] ) && $_GET['page'] == 'pgs-woo-api-support-settings' ) ) {
        wp_enqueue_style( 'jquery-confirm-bootstrap' );
        wp_enqueue_style( 'jquery-confirm' );
        $sts = pgs_woo_api_wp_warning_alert(false);
        if( $sts ){
            wp_localize_script( 'pgs-woo-api-confirm-custom-js', 'pgs_app_confirm_object', array(
                'ajaxurl' => admin_url( 'admin-ajax.php' ),
                'alert_title' => esc_html__( 'Warning', 'pgs-woo-api' ),
                'alert_cancel' => esc_html__( 'Cancel', 'pgs-woo-api' )
            ));
            wp_enqueue_script( 'pgs-woo-api-confirm-custom-js' );
        }
    }

    if( ( isset( $_GET['page'] ) && $_GET['page'] == 'pgs-woo-api-settings' ) ||
        ( isset( $_GET['page'] ) && $_GET['page'] == 'pgs-woo-api-token-settings' ) ) {
        wp_enqueue_style( 'select2', PGS_API_URL . 'css/select2.min.css' );
        wp_enqueue_script( 'select2', PGS_API_URL . 'js/select2.min.js', array('jquery' ) );
        wp_enqueue_style( 'pgs-woo-api-css' );
    }

    if( isset( $_GET['page'] ) && $_GET['page'] == 'pgs-woo-api-support-settings' ){
        wp_enqueue_style( 'pgs-woo-api-support' );
    }

    if( ( isset( $_GET['page'] ) && $_GET['page'] == 'pgs-woo-api-settings' ) ||
        ( isset( $_GET['page'] ) && $_GET['page'] == 'pgs-woo-api-token-settings' ) ||
        ( isset($_GET['taxonomy']) && $_GET['taxonomy'] == 'product_cat') ||
        ( isset($_GET['taxonomy']) && $_GET['taxonomy'] == 'pa_color') ||
        ( isset( $_GET['page'] ) && $_GET['page'] == 'pgs-woo-api-geo-fencing-settings' )
	) {
        wp_enqueue_media();
        wp_enqueue_style( 'thickbox' );
        wp_enqueue_style( 'jquery-ui' );
        wp_enqueue_style( 'wp-color-picker' );
        wp_enqueue_script( 'jquery-repeater-min' );
        wp_enqueue_script( 'jquery-ui-sortable' );
        add_action('wp_before_admin_bar_render', 'pgs_woo_api_remove_toolbar_menu_item_wpml', 999);

        //wp_localize_script( 'pgs-woo-api-js', 'pgs_app_plugin_url', PGS_API_URL );
		$pgs_woo_api_sample_data_required_plugins_list = pgs_woo_api_sample_data_required_plugins_list();
		$plugin_data                                   = pgs_woo_api_get_plugin_data();
		$activated_with                                = pgs_woo_api_activated_with();
		$is_webview_multistep                          = is_webview_multistep_checkout();
		$purchasekey_android                           = pgs_woo_api_get_purchase_key( 'android' );
		$purchasekey_ios                               = pgs_woo_api_get_purchase_key( 'ios' );


        wp_localize_script( 'pgs-woo-api-js', 'pgs_app_sample_data_import_object', array(
			'ajaxurl'                           => admin_url( 'admin-ajax.php' ),
			'alert_title'                       => esc_html__( 'Warning', 'pgs-woo-api' ),
			'alert_proceed'                     => esc_html__( 'Proceed', 'pgs-woo-api' ),
			'alert_cancel'                      => esc_html__( 'Cancel', 'pgs-woo-api' ),
			'alert_install_plugins'             => esc_html__( 'Install Plugins', 'pgs-woo-api' ),
			'alert_default_message'             => esc_html__( 'Importing demo content will import contents, widgets and theme options. Importing sample data will override current widgets and theme options. It can take some time to complete the import process.', 'pgs-woo-api' ),
			'tgmpa_url'                         => admin_url( 'themes.php?page=theme-plugins' ),
			'sample_data_required_plugins_list' => ( ! empty( $pgs_woo_api_sample_data_required_plugins_list) ) ? array_values( $pgs_woo_api_sample_data_required_plugins_list ) : false,
			'sample_import_nonce'               => wp_create_nonce( 'pgs_woo_api_sample_data_security' ),
			'plugin_ver'                        => ( isset($plugin_data['Version']) ) ? $plugin_data['Version'] : '',
			'purchased_android'                 => ( $activated_with['purchased_android'] ) ? $activated_with['purchased_android'] : false,
			'purchased_ios'                     => ( $activated_with['purchased_ios'] ) ? $activated_with['purchased_ios'] : false,
			'purchasekey_android'               => ( isset( $purchasekey_android ) ) ? $purchasekey_android : '',
			'purchasekey_ios'                   => ( isset( $purchasekey_ios ) ) ? $purchasekey_ios : '',
			'checkout_type'                     => $is_webview_multistep,
			'checkout_imge_url_admin'           => trailingslashit( PGS_API_URL ) . 'img',
			'something_went_wrong'              => esc_html__( 'Something went wrong!', 'pgs-woo-api' ),
        ));
        wp_enqueue_script( 'jquery-confirm' );
        wp_enqueue_script( 'pgs-ace-editor-js' );
        wp_enqueue_script( 'pgs-woo-api-js' );
    }
}
add_action( 'admin_enqueue_scripts', 'pgs_woo_script_style_admin' );

/**
 * Enqueue styles for App Checkout page.
 * */
function ciyashop_app_checkout_scripts() {
    $checkout_page = get_option('pgs_checkout_page');
    if(isset($checkout_page) && !empty($checkout_page)){
        $lang='';
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

    if(isset($appcheckout) && !empty($appcheckout)){
        if ( is_page( $appcheckout ) ) {
            wp_enqueue_style( 'ciyashop-app-checkout-style', PGS_API_URL.'css/app-checkout.css');
            $custom_css = get_option('pgs_woo_api_checkout_custom_css');
            // Add custom CSS
        	if(isset($custom_css) && !empty($custom_css)){
        		$custom_css = trim(strip_tags($custom_css));
        		wp_add_inline_style( 'ciyashop-app-checkout-style', $custom_css );
        	}
        }
	}
}
add_action( 'wp_enqueue_scripts', 'ciyashop_app_checkout_scripts' );

function pgs_woo_api_get_plugin_data(){
    global $pgs_woo_api_globals;
    $plugin_data = get_plugin_data( PGS_WOO_API_PLUGIN_FILE );
    $plugin_data['pgs_plugin_slug']  = sanitize_title($plugin_data['Name']);
	$plugin_data['pgs_plugin_name']  = str_replace('-', '_', sanitize_title($plugin_data['Name']));
	$plugin_data['pgs_plugin_option']= $plugin_data['pgs_plugin_name'].'_options';
    return $plugin_data;
}

function pgs_woo_api_get_google_map_api_key(){
    $google_map_api_key = '';
    $pgs_google_keys = get_option('pgs_google_keys');
    if(isset($pgs_google_keys['google_keys']['google_map_api_key']) && !empty($pgs_google_keys['google_keys']['google_map_api_key'])){
        $google_map_api_key = $pgs_google_keys['google_keys']['google_map_api_key'];
    }
    return $google_map_api_key;
}

/**
 * Get app color for app checkput page.
 */
function pgs_woo_api_get_app_color(){
    $app_color = array(
		'primary_color' => '#60A727',
		'secondary_color' => ''
	);
    $app_assets = get_option('pgs_woo_api_app_assets_options');
    if(isset($app_assets) && !empty($app_assets)){
        if(isset($app_assets['app_assets']['app_color']) && !empty($app_assets['app_assets']['app_color'])){
            $app_color = $app_assets['app_assets']['app_color'];
        }
    }
    return $app_color;
}
/**
 * Filter for call custom checkout page
 */
add_filter( 'page_template', 'pgs_woo_api_app_checkout_page' );
function pgs_woo_api_app_checkout_page( $page_template ){

    $checkout_page = get_option('pgs_checkout_page');
    if(isset($checkout_page) && !empty($checkout_page)){
        $lang='';
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
    if(isset($appcheckout) && !empty($appcheckout)){
        if ( is_page( $appcheckout ) ) {
            add_filter('woocommerce_is_checkout',function (){ return true; });
            $is_pgs_multisteps = is_pgs_multisteps_checkout_active();
            if($is_pgs_multisteps){
                PGS_WOO_API_MSCheckout();
            }
            $page_template = PGS_API_PATH . 'template/app_checkout.php';
        }
    }

	return apply_filters( 'pgs_woo_api_app_checkout_page', $page_template );
}

/**
 * Filter for call web view page
 */

add_filter( 'page_template', 'pgs_woo_api_app_web_view_page' );
function pgs_woo_api_app_web_view_page( $page_template ){
$pgs_woo_api_home_option = get_option('pgs_woo_api_home_option');
	if(!empty($pgs_woo_api_home_option))
	{
		$current_id = get_the_id();
		if(isset($pgs_woo_api_home_option['web_view_pages']) && !empty($pgs_woo_api_home_option['web_view_pages']) )
		{
			$web_view_pages = $pgs_woo_api_home_option['web_view_pages'];
			$web_view_pages_ids = array_column( $web_view_pages, 'web_view_pages_page_id' );
			if( in_array( $current_id, $web_view_pages_ids ) ) {
				$page_template = PGS_API_PATH . 'template/web_view.php';
			}
		}
	}
	return $page_template;
}
/*
function pgs_woo_api_app_get_web_view_page_list(){
$pgs_woo_api_home_option = get_option('pgs_woo_api_home_option');
	$web_view_list = array();
	if(!empty($pgs_woo_api_home_option))
	{
		if(isset($pgs_woo_api_home_option['web_view_pages']) && !empty($pgs_woo_api_home_option['web_view_pages']) )
		{
			$web_view_pages = $pgs_woo_api_home_option['web_view_pages'];
		}
	}
	return $page_template;
}*/

function pgs_woo_api_get_app_checkout_page(){
    global $woocommerce;
    $checkout_url = '';
    // $checkout_url = wc_get_checkout_url();
    $checkout_page = get_option('pgs_checkout_page');
    if(isset($checkout_page) && !empty($checkout_page)){
        $lang='';
        $is_wpml_active = pgs_woo_api_is_wpml_active();
        if($is_wpml_active){
            $lang = pgs_woo_api_wpml_get_lang();
            if(!empty($lang)){
                if( defined( 'ICL_LANGUAGE_CODE' ) && function_exists( 'icl_object_id' ) ) {
                    $checkout_page = icl_object_id( $checkout_page, 'post', true, $lang );
                }
            }
        }
        $checkouturl = get_permalink($checkout_page);
		$checkouturl_translated = ( function_exists( 'wpml_permalink_filter' ) ) ? wpml_permalink_filter( $checkouturl, ICL_LANGUAGE_CODE ) : $checkouturl;
        if(isset($checkouturl_translated ) && !empty($checkouturl_translated )){
            $checkout_url = $checkouturl_translated;

			global $WOOCS;
			if( $WOOCS ){
				$current_currency = ( isset($_REQUEST['currency']) && ! empty( $_REQUEST['currency'] ) ) ? strtoupper( sanitize_key( $_REQUEST['currency'] ) ) : $WOOCS->default_currency;
				$checkout_url = add_query_arg( array(
					'currency' => $current_currency,
				), $checkout_url );
			}
        }
    }
    return $checkout_url;
}

function pgs_woo_api_get_app_thankyou_page_endpoint(){
    $endpoint = 'order-received';
    $thankyou_endpoint = get_option( 'woocommerce_checkout_order_received_endpoint' );
    if(isset($thankyou_endpoint) && !empty($thankyou_endpoint)){
        $endpoint = $thankyou_endpoint;
    }
    return esc_html($endpoint);
}

function pgs_woo_api_get_page_title_for_slug($page_slug) {

     $page = get_page_by_path( $page_slug , OBJECT );

     if ( isset($page) )
        return $page;
     else
        return false;
}


function pgs_woo_api_get_all_woo_cat(){
    $taxonomy     = 'product_cat';
    $orderby      = 'name';
    $show_count   = 0;      // 1 for yes, 0 for no
    $pad_counts   = 0;      // 1 for yes, 0 for no
    $hierarchical = 1;      // 1 for yes, 0 for no
    $title        = '';
    $empty        = 0;

    $args = array(
         'taxonomy'     => $taxonomy,
         'orderby'      => $orderby,
         'show_count'   => $show_count,
         'pad_counts'   => $pad_counts,
         'hierarchical' => $hierarchical,
         'title_li'     => $title,
         'hide_empty'   => $empty
    );
    $all_categories = get_categories( $args );
    $opt = array();
    foreach ($all_categories as $cat) {
        if($cat->category_parent == 0) {
            $category_id = $cat->term_id;
            $opt[$category_id] = $cat->name;

            $args2 = array(
                    'taxonomy'     => $taxonomy,
                    'child_of'     => 0,
                    'parent'       => $category_id,
                    'orderby'      => $orderby,
                    'show_count'   => $show_count,
                    'pad_counts'   => $pad_counts,
                    'hierarchical' => $hierarchical,
                    'title_li'     => $title,
                    'hide_empty'   => $empty
            );
            $sub_cats = get_categories( $args2 );
            if($sub_cats) {
                foreach($sub_cats as $sub_category) {
                    $opt[$sub_category->term_id] = "- ".$sub_category->name;
                }
            }
        }
    }
    return $opt;
}
function pgs_woo_api_get_all_woo_product(){
	$opt = array();
	$paged = (get_query_var('paged')) ? get_query_var('paged') : 1;
    $args = array(
		'post_type'     => 'product',
		'paged'         => $paged,
		'order'         => 'ASC',
		'orderby'       => 'title',
		'posts_per_page'=> 5,
    );
    $query = new WP_Query($args);
	if ( $query->have_posts() ) {
		while ( $query->have_posts() ) {
			$query->the_post();
			$opt[$query->post->ID] = $query->post->post_title;
        }
        wp_reset_postdata();
	}
	return $opt;
}

add_action( 'wp_ajax_pgs_woo_api_get_customsection_products', 'pgs_woo_api_get_customsection_products' );
function pgs_woo_api_get_customsection_products(){
    $opt      = array();
    $paged    = ( isset( $_GET['page'] ) ) ? $_GET['page'] : 1;
    $search   = ( isset( $_GET['search'] ) ) ? $_GET['search'] : '';
    $per_page = 20;

	global $sitepress;

	if ( $sitepress ) {
		$default_lang = $sitepress->get_default_language();
		$current_lang = $sitepress->get_current_language();

		//changes to the default language
		$sitepress->switch_lang( $default_lang );
	}

    $args = array(
		'post_type'        => 'product',
		'order'            => 'ASC',
		'orderby'          => 'title',
		'paged'            => $paged,
		'posts_per_page'   => $per_page,
		'suppress_filters' => false,
    );

    if ( ! empty( $search ) ) {
        $args['s'] = $search;
    }

    $query = new WP_Query($args);

	if ( $sitepress ) {
		//changes to the current language
		$sitepress->switch_lang( $current_lang );
	}

    $return['items']  = array();
    $return['pagination'] = false;
	if ( $query->have_posts() ) {
		while ( $query->have_posts() ) {
			$query->the_post();
            //$image = wp_get_attachment_image_src( get_post_thumbnail_id( $query->post->ID ), 'single-post-thumbnail' );

            if ( has_post_thumbnail( $query->post->ID ) ) {
                $img_url = get_the_post_thumbnail_url( $query->post->ID,'thumbnail');
            } else {
                $img_url = wc_placeholder_img_src();
            }
            $wcp = wc_get_product( $query->post->ID );
            $price_html = $wcp->get_price_html();


            $title = $query->post->post_title;
            //$html  = $img . ' ' . $title;
            $json[] = array(
				'id'    => $query->post->ID,
                'text'  => $title,
                'img'   => $img_url,
                'price' => $price_html
            );

        }
        $return['items']  = $json;
        $more = (( $paged * $per_page ) >= $query->found_posts ) ? false: true;
        $return['pagination'] = $more;
    }
    echo json_encode( $return );
    exit();
}

/**
 * Notification message
 */
function pgs_woo_api_admin_notice_render($message,$status) {
    $html = '<div class="notice notice-'.$status.' is-dismissible">';
        $html .= '<p>'.$message.'</p>';
    $html .= '</div>';
    return $html;
}

/**
 * Notification message For AJAX
 */
function pgs_woo_api_ajax_admin_notice_render($message,$status) {
    $html = '<div class="pgs-woo-api-notice pgs-alert-'.$status.'">';
        $html .= '<p>'.$message.'</p>';
    $html .= '</div>';
    return $html;
}

/**
 * Get feature box option status
 */
function pgs_woo_api_feature_box_status($lang=''){
    $pgs_woo_api_home_option = get_option('pgs_woo_api_home_option');
    $feature_box_status = (isset($pgs_woo_api_home_option['feature_box_status']) && !empty($pgs_woo_api_home_option['feature_box_status']))?$pgs_woo_api_home_option['feature_box_status']:'enable';

    if(!empty($lang)){
        $pgs_woo_api_home_option_lang = get_option('pgs_woo_api_home_option_'.$lang);
        $feature_box_status = (isset($pgs_woo_api_home_option_lang['feature_box_status']) && !empty($pgs_woo_api_home_option_lang['feature_box_status']))?$pgs_woo_api_home_option_lang['feature_box_status']:'enable';
    }
    if($feature_box_status == 'enable'){
        $style = 'style="display: block;"';
    } else {
        $style = 'style="display: none;"';
    }
    echo $style;
}

/**
 * Get whatsapp floating button status
 */
function pgs_woo_api_whatsapp_floating_button_status(){
    $pgs_woo_api_home_option = get_option('pgs_woo_api_home_option');
    $whatsapp_floating_button_status = 'disable';
    if(isset($pgs_woo_api_home_option['pgs_app_contact_info']['whatsapp_floating_button'])){
        $whatsapp_floating_button_status = $pgs_woo_api_home_option['pgs_app_contact_info']['whatsapp_floating_button'];
    }

    if($whatsapp_floating_button_status == 'enable'){
        $style = 'style="display: block;"';
    } else {
        $style = 'style="display: none;"';
    }
    echo $style;
}

/**
 * Send checkout page url for android
 * */
add_action( 'wp_footer' , 'pgs_woo_api_add_to_cart_android' );
function pgs_woo_api_add_to_cart_android(){

    $input = file_get_contents("php://input");
    $request = json_decode($input,true);

    if ( isset( $request['cart_items'] ) ) {
		$cart_items = $request['cart_items'];
	}
    if(isset($request['os']) && $request['os'] == 'android'){
        if(isset($cart_items)){
            $url = pgs_woo_api_get_app_checkout_page();
            ?>
            <script type="text/javascript">
                showAndroidToast( '<?php echo esc_url($url)?>');
                function showAndroidToast(toast) {
                    Android.showToast(toast);
                }
            </script><?php
        }
    }
}

function token_generations_pro(){
	$url           = home_url('wp-json');
	$method        = 'GET';
	$pgs_woo_api   = get_option('app_auth');
	$client_key    = '';
	$client_secret = '';
	$token         = '';
	$token_secret  = '';

    if(isset($pgs_woo_api['pgs_auth']) && !empty($pgs_woo_api['pgs_auth'])){
        $pgs_auth = $pgs_woo_api;
        $client_key = (isset($pgs_auth['pgs_auth']['client_key']))?$pgs_auth['pgs_auth']['client_key']:'';
        $client_secret = (isset($pgs_auth['pgs_auth']['client_secret']))?$pgs_auth['pgs_auth']['client_secret']:'';
    }
    if( $client_key != '' && $client_secret != '' ){
        $auth_data = array(
    		'oauth_consumer_key'    => $client_key,
    		'oauth_consumer_secret' => $client_secret,
    	);
    	$step           = 0;
        $oauth_verifier = '';

        if(isset($_POST['step']) && $_POST['step'] == 1){
            $step = $_POST['step'];
            $url  = home_url('wp-json');
		} elseif(isset($_POST['step']) && $_POST['step'] == 2){
			$step                  = $_POST['step'];
			$oauth_verifier        = trim($_POST['oauth_verifier']);
			$oauth_consumer_key    = $client_key;
			$oauth_consumer_secret = $client_secret;
			$oauth_token           = $_POST['oauth_token'];
			$oauth_token_secret    = $_POST['oauth_token_secret'];
			$auth_data             = array(
				'oauth_consumer_key'    => $oauth_consumer_key,
				'oauth_consumer_secret' => $oauth_consumer_secret,
				'oauth_token'           => $oauth_token,
				'oauth_token_secret'    => $oauth_token_secret,
			);
			$url = home_url('oauth1/access');
		}
        if(isset($_POST)){
            $tokenGenerations = new PGS_WOO_API_Token_Generations_Controller( $auth_data, $url, $method, $step, $oauth_verifier  );
        }
    } else {
        esc_html_e('Please enter client key, client secret from Users -> Applications','pgs-woo-api');
    }
}

/**
 * Call test api
 */
add_action( 'wp_ajax_pgs_woo_api_test_api_ajax_call', 'pgs_woo_api_test_api_ajax_call' );
function pgs_woo_api_test_api_ajax_call(){
    echo do_shortcode( '[pgs_woo_api_check_oauth_connection]' );
    exit();
}

/*Plugin info*/
function pgs_woo_api_get_plugin_info(){
	$path        = PGS_WOO_API_PLUGIN_FILE;
	$plugin      = get_plugin_data( $path );
	$plugin_name = $plugin['Name'];
	$plugin_v    = $plugin['Version'];
	$plugin_info = array(
        'name' => $plugin_name,
        'slug' => sanitize_file_name(strtolower($plugin_name)),
        'v'    => $plugin_v,
    );
    return $plugin_info;
}

/**
 * Get validated item purchesh key.
 */
/*function pgs_woo_api_get_verify_product_key() {

    $pgs_token_android = get_option('pgs_woo_api_pgs_token_android');
    $pgs_token_ios = get_option('pgs_woo_api_pgs_token_ios');
    $pgs_product_key = '';
    if( $pgs_token_android && !empty($pgs_token_android)){
        $pgs_product_key = 'c7ec1dc95001d57cdedfe122569648dc';
    } elseif( $pgs_token_ios && !empty( $pgs_token_ios ) ) {
        $pgs_product_key = '7884626eb301b0f657bb23894fd2dbfe';
    }
    return $pgs_product_key;
}*/

/**
 * Check item validated with purchesh key or not.
 */
function pgs_woo_api_is_activated() {
    $pgs_token_android = get_option('pgs_woo_api_pgs_token_android');
    $pgs_token_ios = get_option('pgs_woo_api_pgs_token_ios');

    if( $pgs_token_android && !empty($pgs_token_android)){
		return $pgs_token_android;
	} elseif( $pgs_token_ios && !empty($pgs_token_ios)){
        return $pgs_token_ios;
    }
	return false;
}
/**
 * Check item validate with android or iOS
 */
function pgs_woo_api_activated_with() {
    $pgs_token_android = get_option('pgs_woo_api_pgs_token_android');
    $pgs_token_ios = get_option('pgs_woo_api_pgs_token_ios');
    $purchased_android = false;
    $purchased_ios = false;
    $result = array();
    if( isset($pgs_token_android) && !empty($pgs_token_android)){
    	$purchased_android = true;
    }
    if( isset($pgs_token_ios) && !empty($pgs_token_ios)){
        $purchased_ios = true;
    }
    $result = array(
        'purchased_android' => $purchased_android,
        'purchased_ios' => $purchased_ios
    );
    return $result;
}

/**
 * Check item validate with android or iOS
 */
function pgs_woo_api_get_purchase_key( $with = 'android' ) {
    if ( 'android' == $with ) {
        $purchase_key = get_option('pgs_woo_api_plugin_android_purchase_key');
    } else {
        $purchase_key = get_option('pgs_woo_api_plugin_ios_purchase_key');
    }
    return $purchase_key;
}


/**
 * Allowed html for language file translation etc
 */
function pgs_woo_api_allowed_html( $allowed_els = '' ){

	// bail early if parameter is empty
	if( empty($allowed_els) ) return array();

	if( is_string($allowed_els) ){
		$allowed_els = explode(',', $allowed_els);
	}

	$allowed_html = array();

	$allowed_tags = wp_kses_allowed_html('post');

	foreach( $allowed_els as $el ){
		$el = trim($el);
		if( array_key_exists($el, $allowed_tags) ){
			$allowed_html[$el] = $allowed_tags[$el];
		}
	}
	return $allowed_html;
}

/**
 * Widzard process check plugin active
 */
function pgs_woo_api_widzard_check_plugin_active( $plugin = '' ) {

	if( empty($plugin) ) return false;

	return ( in_array( $plugin, (array) get_option( 'active_plugins', array() ) ) || ( function_exists('is_plugin_active_for_network') && is_plugin_active_for_network($plugin) ) );
}
/**
 * Check item token is activated with purches key
 */
function pgs_woo_api_token_is_activated() {
	$pgs_token_android = get_option('pgs_woo_api_pgs_token_android');
    $pgs_token_ios = get_option('pgs_woo_api_pgs_token_ios');

    if( $pgs_token_android && !empty($pgs_token_android)){
		return $pgs_token_android;
	} elseif( $pgs_token_ios && !empty($pgs_token_ios)){
        return $pgs_token_ios;
    }
	return false;
}


// Load the auto-update class
add_action( 'init', 'pgs_woo_api_activate_au' );
function pgs_woo_api_activate_au(){
	global $plugin_version;

	require_once ( PGS_API_PATH.'inc/classes/class-pgs-wp-autoupdate.php' );
	$auth_token = PGS_WOO_API_Support::pgs_woo_api_verify_plugin();
    $product_key = PGS_WOO_API_Support::pgs_woo_api_verify_product_key();
    $current_plugin = pgs_woo_api_get_plugin_data();
	$plugin_current_version = $plugin_version;
	$plugin_remote_path     = trailingslashit(PGS_ENVATO_API).'get-plugin-info';
	$plugin_slug            = 'pgs-woo-api/pgs-woo-api.php';
	$token                  = $auth_token;
	$product_key            = $product_key;
	$site_url               = get_site_url();
    $purchase_key = PGS_WOO_API_Support::pgs_woo_api_get_purchase_key();
    $dd = new Pgs_woo_api_WP_AutoUpdate ( $plugin_current_version, $plugin_remote_path, $plugin_slug, $token, $product_key, $site_url,$purchase_key);
}

/**
 * Remove admin bar to App Checkout page
 */
function pgs_woo_api_remove_admin_bar() {
    show_admin_bar(false);
}
/**
 * Get contact us email option data
 */
function pgs_woo_api_get_contact_mail_options_data() {
    $admin_email = get_bloginfo( 'admin_email' );
    $woocommerce_email_from_name = get_option('woocommerce_email_from_name');
    $from_name = (isset($woocommerce_email_from_name))?$woocommerce_email_from_name:$site_name;
    $woocommerce_email_from_address = get_option('woocommerce_email_from_address');
    $from_email = (isset($woocommerce_email_from_address))?$woocommerce_email_from_address:$admin_email;
    $contact_us_recipient = trim(sanitize_text_field($from_email));
    $contact_us_from_name = $from_name;
    $contact_us_from_email = $from_email;

    $option_contact_recipient = get_option('pgs_woo_api_emails_contact_recipient');
    $option_contact_from_name = get_option('pgs_woo_api_emails_contact_from_name');
    $option_contact_from_address = get_option('pgs_woo_api_emails_contact_from_address');
    $contact_us_recipient = (!empty($option_contact_recipient))?$option_contact_recipient:$contact_us_recipient;
    $contact_us_from_name = (!empty($option_contact_from_name))?$option_contact_from_name:$contact_us_from_name;
    $contact_us_from_email = (!empty($option_contact_from_address))?$option_contact_from_address:$contact_us_from_email;
    return array(
        'contact_us_recipient' => $contact_us_recipient,
        'contact_us_from_name' => $contact_us_from_name,
        'contact_us_from_email' => $contact_us_from_email
    );
}
/**
 * Get forgot password us email option data
 */
function pgs_woo_api_get_forgot_password_mail_options_data() {
    $site_name = get_bloginfo( 'name' );
    $admin_email = get_bloginfo( 'admin_email' );
    $woocommerce_email_from_name = get_option('woocommerce_email_from_name');
    $from_name = (isset($woocommerce_email_from_name))?$woocommerce_email_from_name:$site_name;
    $woocommerce_email_from_address = get_option('woocommerce_email_from_address');
    $from_email = (isset($woocommerce_email_from_address))?$woocommerce_email_from_address:$admin_email;
    $forgot_password_from_name = $from_name;
    $forgot_password_from_email = $from_email;

    if ( is_multisite() ){
        $blogname = $GLOBALS['current_site']->site_name;
    } else {
        $blogname = wp_specialchars_decode(get_option('blogname'), ENT_QUOTES);
    }
    $subject = sprintf( __( '[%s] Password Reset', 'pgs-woo-api' ), $blogname );

    $option_forgot_password_subject = get_option('pgs_woo_api_emails_forgot_password_subject');
    $option_forgot_password_from_name = get_option('pgs_woo_api_emails_forgot_password_from_name');
    $option_forgot_password_from_address = get_option('pgs_woo_api_emails_forgot_password_address');
    $subject = (!empty($option_forgot_password_subject))?$option_forgot_password_subject:$subject;
    $forgot_password_from_name = (!empty($option_forgot_password_from_name))?$option_forgot_password_from_name:$forgot_password_from_name;
    $forgot_password_from_email = (!empty($option_forgot_password_from_address))?$option_forgot_password_from_address:$forgot_password_from_email;

    return array(
        'forgot_password_subject' => $subject,
        'forgot_password_from_name' => $forgot_password_from_name,
        'forgot_password_from_email' => $forgot_password_from_email
    );
}

/**
 * Get vendor contact email option data
 */
function pgs_woo_api_get_vendor_contact_mail_options_data() {
    $site_name = get_bloginfo( 'name' );
    $admin_email = get_bloginfo( 'admin_email' );
    $woocommerce_email_from_name = get_option('woocommerce_email_from_name');
    $from_name = (isset($woocommerce_email_from_name))?$woocommerce_email_from_name:$site_name;
    $woocommerce_email_from_address = get_option('woocommerce_email_from_address');
    $from_email = (isset($woocommerce_email_from_address))?$woocommerce_email_from_address:$admin_email;
    $vendor_contact_from_name = $from_name;
    $vendor_contact_from_email = $from_email;

    $option_vendor_contact_subject = get_option('pgs_woo_api_emails_vendor_contact_subject');
    $option_vendor_contact_from_name = get_option('pgs_woo_api_emails_vendor_contact_from_name');
    $option_vendor_contact_from_address = get_option('pgs_woo_api_emails_vendor_contact_address');


    $subject = (!empty($option_vendor_contact_subject))?$option_vendor_contact_subject:'';
    $vendor_contact_from_name = (!empty($option_vendor_contact_from_name))?$option_vendor_contact_from_name:$vendor_contact_from_name;
    $vendor_contact_from_email = (!empty($option_vendor_contact_from_address))?$option_vendor_contact_from_address:$vendor_contact_from_email;

    return array(
        'vendor_contact_subject' => $subject,
        'vendor_contact_from_name' => $vendor_contact_from_name,
        'vendor_contact_from_email' => $vendor_contact_from_email
    );
}
/**
 * Get vendor contact email option data
 */
function pgs_woo_api_wpml_get_lang( ){
    $lang = '';
    if(isset($_GET['lang']) && !empty($_GET['lang'])){
        $pgs_woo_api_wpml_initial_language = get_option('pgs_woo_api_wpml_initial_language_array');
        $icl_get_languages = icl_get_languages();
        if(!empty($icl_get_languages)){
            if(isset($icl_get_languages[$_GET['lang']])){
                $lang_id = $icl_get_languages[$_GET['lang']]['id'];
                if($lang_id !== $pgs_woo_api_wpml_initial_language['id']){
                    $lang=$_GET['lang'];
                }
            }
        }
    } else {
        global $sitepress;
        $default_lang = $sitepress->get_default_language();
        $pgs_woo_api_wpml_initial_language = get_option('pgs_woo_api_wpml_initial_language_array');
        $icl_get_languages = icl_get_languages();
        $lang = $default_lang;
        if(!empty($icl_get_languages)){
            if(isset($icl_get_languages[$default_lang])){
                $lang_id = $icl_get_languages[$default_lang]['id'];
                if($lang_id !== $pgs_woo_api_wpml_initial_language['id']){
                    $lang = $default_lang;
                } else {
                    $lang = '';
                }
            }
        }
    }
    return $lang;
}
/**
 * Remove admin toolbar menu item for WPML
 */
function pgs_woo_api_remove_toolbar_menu_item_wpml() {
	global $wp_admin_bar;
	$wp_admin_bar->remove_menu('WPML_ALS_all');
}
/**
 * Get products carousel data for home API and options page
 */
function pgs_woo_api_get_products_carousel(){
    $pgs_woo_api_home_option = get_option('pgs_woo_api_home_option');
    $lang='';
    $is_wpml_active = pgs_woo_api_is_wpml_active();
    if($is_wpml_active){
        $lang = pgs_woo_api_wpml_get_lang();
        if(!empty($lang)){
            $lang_prifix = '_'.$lang;
            $products_carousel = get_option('pgs_woo_api_home_option'.$lang_prifix);
            if(isset($products_carousel['products_carousel']) && !empty($products_carousel['products_carousel'])){
                $pgs_woo_api_home_option = $products_carousel;
            }

        }
    }

    $feature_doc_description = sprintf(
        // %s = Link to documentation
        wp_kses( __( 'How to create featured product <a href="%s" target="_blank">click here</a>.', 'pgs-woo-api' ),
    		array(
    			'a'    => array(
    				'href' => array(),
                    'target' => array(),
    			),
    		)
        ),
        'https://docs.woocommerce.com/document/managing-products/#section-20'
    );
    $special_deal_doc_description = sprintf(
        // %s = Link to documentation
        wp_kses( __( 'How to create special deal products <a href="%s" target="_blank">click here</a>.', 'pgs-woo-api' ),
    		array(
    			'a'    => array(
    				'href' => array(),
                    'target' => array(),
    			),
    		)
        ),
        'https://docs.potenzaglobalsolutions.com/docs/new-design-ciyashop-android/?section=special-deal-products-or-schedule-sale-products-3'
    );
    $recent_doc_description = esc_html__( 'It will show recently added product list.', 'pgs-woo-api' );
    $popular_doc_description = esc_html__( 'It will show the products list based on total sales.', 'pgs-woo-api' );
    $top_rated_products = esc_html__( 'It will show the products list based on top rating.', 'pgs-woo-api' );


    $products_carousel_default = array(
        'feature_products' => array(
            'label' => esc_html__("Feature Products",'pgs-woo-api'),
            'description' => esc_html__('Feature products view display on the home screen.','pgs-woo-api'),
            'doc_description' => '<p class="description">'.$feature_doc_description.'</p>',
            'status' => "enable",
            'title' => "Feature products"
        ),
        'recent_products' => array(
            'label' => esc_html__("Recent Products",'pgs-woo-api'),
            'description' => esc_html__("Recent products view display on the home screen.",'pgs-woo-api'),
            'doc_description' => '<p class="description">'.$recent_doc_description.'</p>',
            'status' => "enable",
            'title' => "Recent products"
        ),
        'special_deal_products' => array(
            'label' => esc_html__("Special Deal Products",'pgs-woo-api'),
            'description' => esc_html__("Special deal products view display on the home screen.",'pgs-woo-api'),
            'doc_description' => '<p class="description">'.$special_deal_doc_description.'</p>',
            'status' => "enable",
            'title' => "Special deal"
        ),
        'popular_products' => array(
            'label' => esc_html__("Popular Products",'pgs-woo-api'),
            'description' => esc_html__("Popular products view display on the home screen.",'pgs-woo-api'),
            'doc_description' => '<p class="description">'.$popular_doc_description.'</p>',
            'status' => "enable",
            'title' => "Popular products"
        ),
        'top_rated_products' => array(
            'label' => esc_html__("Top Rated Products",'pgs-woo-api'),
            'description' => esc_html__("Top Rated products view display on the home screen.",'pgs-woo-api'),
            'doc_description' => '<p class="description">'.$top_rated_products.'</p>',
            'status' => "enable",
            'title' => "Top Rated products"
        )
    );
    $products_carousel['products_carousel'] = $products_carousel_default;
    $product_view_array = array(
        'feature_products','recent_products','special_deal_products','popular_products','top_rated_products'
    );
    foreach($product_view_array as $v){
        $products_carousel['products_carousel'][$v]['label'] = $products_carousel_default[$v]['label'];
        $products_carousel['products_carousel'][$v]['description'] = $products_carousel_default[$v]['description'];
        $products_carousel['products_carousel'][$v]['doc_description'] = $products_carousel_default[$v]['doc_description'];

        $status = $products_carousel_default[$v]['status'];
        if(isset($pgs_woo_api_home_option['products_carousel'][$v]['status'])){
            $status = $pgs_woo_api_home_option['products_carousel'][$v]['status'];
        }
        $title = $products_carousel_default[$v]['title'];
        if(isset($pgs_woo_api_home_option['products_carousel'][$v]['title'])){
            $title = $pgs_woo_api_home_option['products_carousel'][$v]['title'];
        }
        $products_carousel['products_carousel'][$v]['status'] = $status;
        $products_carousel['products_carousel'][$v]['title'] = $title;
    }

    if(isset($pgs_woo_api_home_option['products_carousel'])){
        $old_productscarousel_key['products_carousel'] = $pgs_woo_api_home_option['products_carousel'];
        if(!in_array( 'top_rated_products', $pgs_woo_api_home_option['products_carousel'])){
            $old_productscarousel_key['products_carousel']['top_rated_products'] = $products_carousel['products_carousel']['top_rated_products'];
        }

        foreach($old_productscarousel_key['products_carousel'] as $k => $v){
            if( $k == 'feature_products' ){
                $new_products_carousel['products_carousel'][$k]['label'] = $products_carousel['products_carousel'][$k]['label'];
                $new_products_carousel['products_carousel'][$k]['description'] = $products_carousel['products_carousel'][$k]['description'];
                $new_products_carousel['products_carousel'][$k]['doc_description'] = $products_carousel['products_carousel'][$k]['doc_description'];
                $new_products_carousel['products_carousel'][$k]['status'] = $products_carousel['products_carousel'][$k]['status'];
                $new_products_carousel['products_carousel'][$k]['title'] = $products_carousel['products_carousel'][$k]['title'];
            } elseif( $k == 'recent_products' ){
                $new_products_carousel['products_carousel'][$k]['label'] = $products_carousel['products_carousel'][$k]['label'];
                $new_products_carousel['products_carousel'][$k]['description'] = $products_carousel['products_carousel'][$k]['description'];
                $new_products_carousel['products_carousel'][$k]['doc_description'] = $products_carousel['products_carousel'][$k]['doc_description'];
                $new_products_carousel['products_carousel'][$k]['status'] = $products_carousel['products_carousel'][$k]['status'];
                $new_products_carousel['products_carousel'][$k]['title'] = $products_carousel['products_carousel'][$k]['title'];
            } elseif( $k == 'special_deal_products' ){
                $new_products_carousel['products_carousel'][$k]['label'] = $products_carousel['products_carousel'][$k]['label'];
                $new_products_carousel['products_carousel'][$k]['description'] = $products_carousel['products_carousel'][$k]['description'];
                $new_products_carousel['products_carousel'][$k]['doc_description'] = $products_carousel['products_carousel'][$k]['doc_description'];
                $new_products_carousel['products_carousel'][$k]['status'] = $products_carousel['products_carousel'][$k]['status'];
                $new_products_carousel['products_carousel'][$k]['title'] = $products_carousel['products_carousel'][$k]['title'];
            } elseif( $k == 'popular_products' ){
                $new_products_carousel['products_carousel'][$k]['label'] = $products_carousel['products_carousel'][$k]['label'];
                $new_products_carousel['products_carousel'][$k]['description'] = $products_carousel['products_carousel'][$k]['description'];
                $new_products_carousel['products_carousel'][$k]['doc_description'] = $products_carousel['products_carousel'][$k]['doc_description'];
                $new_products_carousel['products_carousel'][$k]['status'] = $products_carousel['products_carousel'][$k]['status'];
                $new_products_carousel['products_carousel'][$k]['title'] = $products_carousel['products_carousel'][$k]['title'];
            } elseif( $k == 'top_rated_products' ){
                $new_products_carousel['products_carousel'][$k]['label'] = $products_carousel['products_carousel'][$k]['label'];
                $new_products_carousel['products_carousel'][$k]['description'] = $products_carousel['products_carousel'][$k]['description'];
                $new_products_carousel['products_carousel'][$k]['doc_description'] = $products_carousel['products_carousel'][$k]['doc_description'];
                $new_products_carousel['products_carousel'][$k]['status'] = $products_carousel['products_carousel'][$k]['status'];
                $new_products_carousel['products_carousel'][$k]['title'] = $products_carousel['products_carousel'][$k]['title'];
            }
        }
        $products_carousel['products_carousel'] = $new_products_carousel['products_carousel'];
    }
    return $products_carousel['products_carousel'];
}

/**
 * verify notice for show notice alert chk
 */
add_action('wp_ajax_pgs_woo_api_call_try_now', 'pgs_woo_api_call_try_now');
function pgs_woo_api_call_try_now(){
    $action_url = admin_url( 'admin.php?page=pgs-woo-api-support-settings' );
    if ( ! wp_verify_nonce( $_POST['call_try_now_nonce'], 'pgs_woo_api_call_try_now_security' ) ) {
		$import_status_data = array(
			'success'     => false,
			'message'     => esc_html__( 'Please verify your purchase key!' , 'pgs-woo-api' ),
            'action'      => $action_url
		);
	} else {
        $import_status_data = array(
            'success'     => false,
            'message'     => esc_html__( 'Please verify your purchase key!' , 'pgs-woo-api' ),
            'action'      => $action_url
        );
    }
    wp_send_json( $import_status_data );
    exit();
}

/**
 * wp notice for show notice alert
 */
function pgs_woo_api_wp_warning_alert($count=false) {
    $show_notice = false; $notice = '';
    $show_notice_data = get_option('pgs_woo_api_plugin_show_key_notice');
    $show_notice_transient = get_site_transient("pgs_woo_api_plugin_show_key_notice");
    if( $show_notice_data == true ){
        $show_notice = true;
    } elseif ( $show_notice_transient  == true ){
        $show_notice = true;
    }

    if(isset($_POST['not']) && $_POST['not'] == true ){
        $show_notice = false;
    }
    if( $count == true ){
        $show_count = '';
        if($show_notice){
            $show_notice_count = get_option('pgs_woo_api_plugin_show_key_notice_count');
            $show_notice_transient_count = get_site_transient("pgs_woo_api_plugin_show_key_notice_count");
            if( !empty($show_notice_count) ){
                $show_count = $show_notice_count;
            } elseif( !empty($show_notice_transient_count) ){
                $show_count = $show_notice_transient_count;
            }
            $show_notice_transient_count = get_site_transient("pgs_woo_api_plugin_show_key_notice_count");
        }
        return array( 'show_notice' => $show_notice, 'show_notice_count' => $show_count  );
    } else {
        return $show_notice;
    }
}
/**
 * Template file for show alert contet for show notice alert
 */
function pgs_woo_api_warning_alert_templates() {
    if( (isset($_GET['page']) && $_GET['page'] == "pgs-woo-api-support-settings") ||
        (isset($_GET['page']) && $_GET['page'] == "pgs-woo-api-settings") ||
        ( isset( $_GET['page'] ) && $_GET['page'] == 'pgs-woo-api-token-settings' ) ) {
        $sts = pgs_woo_api_wp_warning_alert();
        if( $sts ){
            include_once trailingslashit(PGS_API_PATH) . "template/warning-alert/pgs-woo-api-warning-alert.php";
        }
	}

}
add_action( "admin_footer", "pgs_woo_api_warning_alert_templates" );

add_filter( 'woocommerce_is_rest_api_request', 'woocommerce_is_rest_api_request_new_pgs_woo_api',10,1 );
function woocommerce_is_rest_api_request_new_pgs_woo_api($data){
    if( strpos( $_SERVER['REQUEST_URI'], 'add_to_cart' ) ){
        $input = file_get_contents("php://input");
        $request = json_decode($input,true);
        if(isset($request['cart_items'])){
            return false;
        }
    }
    return $data;
}

/*
 * wp get all product variations.
 */
add_filter( 'woocommerce_rest_product_variation_object_query', 'woocommerce_rest_product_variation_object_query_new_pgs_woo_api', 10, 2 );
function woocommerce_rest_product_variation_object_query_new_pgs_woo_api( $args, $request ){
	if( ! isset( $_GET['page'] ) ) {
		$args['posts_per_page'] = -1;
	}
	return $args;
}

add_action('pgs_woo_api_promo_notice', 'pgs_woo_api_promo');
if ( !function_exists('pgs_woo_api_promo') ) {
    function pgs_woo_api_promo() {
		if( ! class_exists('PGS_WOO_API_MultiSteps_Checkout') ) {
			$review_time = get_option('ciyashop_notice_pgs_woo_api_promo_time');
			if ($review_time == '') {
				$review_time = time();
				update_option('ciyashop_notice_pgs_woo_api_promo_time', $review_time);
			}
			// Are we to show the promo popup
			if ($review_time != '' && $review_time > 0) {
				echo '
				<script>
				jQuery(document).ready( function() {
						(function($) {
								$("#ciyashop_pgs_woo_api_promo .ciyashop_pgs_woo_api_promo-close").click(function(){
										var data;

										// Hide it
										$("#ciyashop_pgs_woo_api_promo").hide();

										// Save this preference
										$.post("' . admin_url('?ciyashop_pgs_woo_api_promo=0') . '", data, function(response) {
												//alert(response);
										});
								});
						})(jQuery);
				});
				</script>';
				echo '
				<div class="notice notice-success" id="ciyashop_pgs_woo_api_promo" style="min-height:120px">
						<a class="ciyashop_pgs_woo_api_promo-close" href="javascript:" aria-label="Dismiss this Notice">
								<span class="dashicons dashicons-dismiss"></span> Dismiss
						</a>
						<img src="'.esc_url( PGS_API_URL . 'img/multi-step-logo.png').'" style="float:left; margin:10px 20px 10px 10px" width="100" />
						<p style="font-size:16px">' . __("<strong>Multi-Step Checkout Ad On</strong><br/> Now you can have Multi Step checkout in your Application. You can buy and install the plugin in easy steps and enable Multi-step checkout. Please check the video here <a target='_blank' href='https://www.youtube.com/watch?v=8G50TUZAZUg&list=PLplHaPmX0cKVj0z6YMAYTYpUtWVpJZYKa&index=13'>Android </a> & <a target='_blank' href='https://www.youtube.com/watch?v=es3JjgoBrt8&list=PLplHaPmX0cKVj0z6YMAYTYpUtWVpJZYKa&index=12'>iOS</a>", 'pgs-woo-api') . '</p>
						<p>
							<a class="ciyashop_notice_button ciyashop_notice_button2" target="_blank" href="https://services.potenzaglobalsolutions.com/product/ciyashop-mobile-application-multistep-checkout-addon/">' . __("Buy Now", 'pgs-woo-api') . '</a>
						</p>
				</div>';
			} else {
				$time = time();
				if(($time - abs($review_time)) >= 1000000) {
					update_option('ciyashop_notice_pgs_woo_api_promo_time', $time);
				}
			}
		}
    }
}
add_action('after_setup_theme', 'pgs_woo_api_promo_updated');
if (!function_exists('pgs_woo_api_promo_updated')) {
    function pgs_woo_api_promo_updated() {
        // Are we to disable the promo popup
        if (isset($_GET['ciyashop_pgs_woo_api_promo']) && (int) $_GET['ciyashop_pgs_woo_api_promo'] == 0) {
			$time = time();
            update_option('ciyashop_notice_pgs_woo_api_promo_time', (0 - $time));
            die($time);
        }
    }
}

function pgs_woo_api_set_currency() {
	global $WOOCS;
	if( $WOOCS ){
		$current_currency = ( isset($_REQUEST['currency']) && ! empty( $_REQUEST['currency'] ) ) ? strtoupper( sanitize_key( $_REQUEST['currency'] ) ) : $WOOCS->default_currency;
		$WOOCS->set_currency( $current_currency );
	}
}

add_action( 'rest_api_init', 'pgs_woo_api_set_currency_with_rest_api_init' );
function pgs_woo_api_set_currency_with_rest_api_init(){
	pgs_woo_api_set_currency();
}

function pgswooapi_razorpay_cancel_payment_redirect_uri_fix( $return_url, $order ){

	if ( $order ) {
		if ( in_array( $order->get_status(), array( 'cancelled' ) ) ) {
			$return_url = add_query_arg( array(
				'cancel_order' => 'true',
			), $return_url );
		} elseif ( 'razorpay' === $order->get_payment_method() && ( in_array( $order->get_status(), array( 'failed', 'pending' ) ) ) ) { // RazorPay Handling.
			$return_url = add_query_arg( array(
				'cancel_order' => 'true',
			), $return_url );
		}
	}

	return $return_url;
}
add_filter( 'woocommerce_get_return_url', 'pgswooapi_razorpay_cancel_payment_redirect_uri_fix', 10, 2 );

function pgswooapi_get_checkout_order_received_url_with_order_id( $order_received_url, $order ) {

	$order_received_url = add_query_arg( 'app_checkout_order_id', $order->get_id(), $order_received_url );

	return $order_received_url;
}
add_filter( 'woocommerce_get_checkout_order_received_url', 'pgswooapi_get_checkout_order_received_url_with_order_id', 10, 2 );

function pgs_woo_api_lang_object_ids( $object_id, $type ) {
	if ( is_array( $object_id ) ) {
		$translated_object_ids = array();
		foreach ( $object_id as $id ) {
			$translated_object_ids[] = apply_filters( 'wpml_object_id', $id, $type, true, $current_language );
		}
		return $translated_object_ids;
	} else {
		return apply_filters( 'wpml_object_id', $object_id, $type, true, $current_language );
	}
}

/**
 * Clean variables using sanitize_text_field. Arrays are cleaned recursively.
 * Non-scalar values are ignored.
 *
 * @param string|array $var Data to sanitize.
 * @return string|array
 */
function pgs_woo_api_clean( $var ) {
	if ( is_array( $var ) ) {
		return array_map( 'wc_clean', $var );
	} else {
		return is_scalar( $var ) ? sanitize_text_field( $var ) : $var;
	}
}

function pgs_woo_api_delete_account_alert_title() {
	$home_option = get_option( 'pgs_woo_api_home_option' );
	$title       = esc_html__( 'Are you sure you want to delete your account? This action cannot be reversed.', 'pgs-woo-api' );

	if ( isset( $home_option['delete_account_alert_title'] ) && ! empty( $home_option['delete_account_alert_title'] ) ) {
		$title = $home_option['delete_account_alert_title'];
	}

	return $title;
}
function pgs_woo_api_delete_account_alert_message() {
	$home_option = get_option( 'pgs_woo_api_home_option' );
	$message     = esc_html__( 'When you delete your account, you won\'t be able to access/retrieve the transactions, orders, downloads, and information you\'ve on this app/website.', 'pgs-woo-api' );

	if ( isset( $home_option['delete_account_alert_message'] ) && ! empty( $home_option['delete_account_alert_message'] ) ) {
		$message = $home_option['delete_account_alert_message'];
	}

	return $message;
}
