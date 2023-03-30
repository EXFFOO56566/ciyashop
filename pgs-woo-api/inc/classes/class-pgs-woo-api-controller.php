<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}

class PGS_WOO_API_Controller {

    protected $push_table = "pgs_woo_api_notifications";
    protected $push_meta_table = "pgs_woo_api_notifications_meta";
    protected $push_relation_table = "pgs_woo_api_notifications_relationships";

    public function pgs_woo_api_permission_callback() {

        $is_wpml_active = pgs_woo_api_is_wpml_active();
        if($is_wpml_active){
            $lang = pgs_woo_api_wpml_get_lang();
            if(!empty($lang)){
                $switch_lang = $lang;
            } else {
                $current_lang = get_option('pgs_woo_api_wpml_initial_language_array');
                $switch_lang = $current_lang['code'];
            }
            global $sitepress;
            $current_lang = $sitepress->get_current_language();
            if($current_lang != $switch_lang){
                $sitepress->switch_lang($switch_lang);
            }
        }
        return current_user_can( 'administrator' );
    }

    public function pgs_woo_api_app_validation($request) {
        $is_app_validation = $request['is_app_validation'];
        if($is_app_validation){
            $val = true;
        } else {
            $val = false;
        }
        $this->pgs_woo_api_app_val_options($val);
        return true;
    }

    protected function pgs_woo_api_app_val_options($val){
        return update_option('app_validation',$val);
    }

    /**
    * Validate required fields
    */
    protected function pgs_woo_api_param_validation($paramarray,$data) {
    	$novalueParam = array();

        if(isset($data) && !empty($data)){
            $data = $data;
        } else {
            $data = array();
        }

        foreach($paramarray as $val) {
    		if(!array_key_exists($val,$data)) {
    			$novalueParam[] = $val;
    		}
    	}
    	if(is_array($novalueParam) && count($novalueParam)>0) {
    		$returnArr['error'] = "error";
    		$returnArr['message'] = esc_html__('Sorry, that is not valid input. You missed '.implode(',',$novalueParam).' parameters','pgs-woo-api');
    		return $returnArr;
    	} else {
    		return false;
    	}
    }

    /**
     * Update currency rate if currency switcher plugin is active
     */
    public function pgs_woo_api_update_currency_rate($price){

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
     * Send Pushnotification
     */
    public function send_push($msg, $badge, $custom_msg,$not_code,$device_data) {

        $pushstatus = get_option('pgs_push_status');
        $push_status = (isset($pushstatus) && !empty($pushstatus))?$pushstatus:'enable';
        if($push_status != 'enable'){
            return;
        }

        $pushmode = get_option('pgs_push_mode');
        $push_mode = (isset($pushmode) && !empty($pushmode))?$pushmode:'live';
        $filter_array_ios = array();
        $filter_array_ios = array_filter($device_data, array($this,'filter_array_ios_arr'));
        $filter_array_android = array_filter($device_data, array($this,'filter_array_android'));

        if( !empty($filter_array_ios )){
            $this->ios( $filter_array_ios,$msg, $badge, $custom_msg,$not_code,$push_mode );
        }

        if( !empty($filter_array_android )){
            $this->android( $filter_array_android, $msg, $badge, $custom_msg, $not_code );
        }
	}

    /**
    * Send Pushnotification for IOS
    */
    public function ios($devicetokens,$msg, $badge, $custom_msg,$not_code,$push_mode){
        $pem_file = '';
		$pem_file_pass = '';

		if($push_mode == 'sandbox'){
            $http2_server   = 'https://api.development.push.apple.com';
            $pem_file_dev   = get_option('pem_file_dev');
            $pem_file       =  (isset($pem_file_dev))?$pem_file_dev:'';
            $pemfiledevpass = get_option('pem_file_dev_pass');
            $pem_file_pass  = (isset($pemfiledevpass) && !empty($pemfiledevpass))?$pemfiledevpass:'';
        } else {
			$http2_server   = 'https://api.push.apple.com';
            $pem_file_pro   = get_option('pem_file_pro');
            $pem_file       = (isset($pem_file_pro))?$pem_file_pro:'';
            $pemfilepropass = get_option('pem_file_pro_pass');
            $pem_file_pass  = (isset($pemfilepropass) && !empty($pemfilepropass))?$pemfilepropass:'';
		}

        if ( $pem_file != '' ) {

            $upload = wp_upload_dir();
            $pem = $upload['basedir'].'/pgs-woo-api/pem/'.$pem_file;


    		// Create the payload body
    		$body['aps'] = array(
    			'alert' => array(
    			    'title'    => $msg,
                    'body'     => $custom_msg,
                    'badge'    => $badge,
                    'not_code' => $not_code
    			 ),
    			'sound' => 'default'
    		);

    		// Encode the payload as JSON
    		$payload = json_encode($body);

            $lastid = 0;
    		if($not_code != 0){ // check for test notification zero for test notification
                $lastid = $this->add_notification_meta($msg,$custom_msg,$not_code);
    		}

			// this is only needed with php prior to 5.5.24
            if ( ! defined( 'CURL_HTTP_VERSION_2_0' ) ) {
                define( 'CURL_HTTP_VERSION_2_0', 3 );
            }

            $http2ch = curl_init();

			foreach ( $devicetokens as $devicetoken ) {

				$token = $devicetoken['token'];
                $token_ststus = $this->is_notifiction_on_to_token($token);

				if(isset($token_ststus) && $token_ststus == 1){
                    curl_setopt($http2ch, CURLOPT_HTTP_VERSION, CURL_HTTP_VERSION_2_0);

                    // send push
                    $apple_cert = $pem;
                    $message = $payload;
                    $status = $this->send_http2_push($http2ch, $http2_server, $apple_cert, $message, $token);
                    //echo "Response from apple -> {$status}\n";
                }

                if ( $not_code != 0 ) {
                    $this->add_push_relation($token,$lastid);
                }
    		}
            // close connection
            curl_close($http2ch);

        }
    }

	/**
     * @param $http2ch          the curl connection
     * @param $http2_server     the Apple server url
     * @param $apple_cert       the path to the certificate
     * @param $app_bundle_id    the app bundle id
     * @param $message          the payload to send (JSON)
     * @param $token            the token of the device
     * @return mixed            the status code
     */
    public function send_http2_push($http2ch, $http2_server, $apple_cert, $message, $token) {

        // url (endpoint)
        $url = "{$http2_server}/3/device/{$token}";

        // certificate
        $cert = realpath($apple_cert);

        // headers
        /*$headers = array(
            "apns-topic: {$app_bundle_id}",
            "User-Agent: CiyaShop App"
        );*/

        // other curl options
        curl_setopt_array($http2ch, array(
            CURLOPT_URL => $url,
            CURLOPT_PORT => 443,
            //CURLOPT_HTTPHEADER => $headers,
            CURLOPT_POST => TRUE,
            CURLOPT_POSTFIELDS => $message,
            CURLOPT_RETURNTRANSFER => TRUE,
            CURLOPT_TIMEOUT => 30,
            CURLOPT_SSL_VERIFYPEER => false,
            CURLOPT_SSLCERT => $cert,
            CURLOPT_HEADER => 1
        ));

        // go...
        $result = curl_exec($http2ch);
        if ($result === FALSE) {
        	throw new Exception("Curl failed: " .  curl_error($http2ch));
        }

        // get response
        $status = curl_getinfo($http2ch, CURLINFO_HTTP_CODE);

        return $status;
    }


    /**
    * Sends Push notification for Android users
    */
    public function android( $devicetokens,$msg, $badge, $custom_msg,$not_code ) {

        $android_l_s_key = get_option('android_l_s_key');
        $android_key = (isset($android_l_s_key))?$android_l_s_key:'';
        if($android_key != ''){
            $url = 'https://fcm.googleapis.com/fcm/send';
            $message = array(
                'title' => $msg,
                'body' => $custom_msg
            );

            $data = array(
				'title' => $msg,
                'message' => $custom_msg,
                'not_code' => $not_code
			);

            $headers = array(
            	'Authorization: key=' .$android_key,
            	'Content-Type: application/json'
            );



            $lastid = 0;
    		if($not_code != 0){ // check for test notification zero for test notification
                $lastid = $this->add_notification_meta($msg,$custom_msg,$not_code);
    		}
            foreach($devicetokens as $devicetoken){
                $token = $devicetoken['token'];
                $token_ststus = $this->is_notifiction_on_to_token($token);
                if(isset($token_ststus) && $token_ststus == 1){
                    $fields = array(
                        'registration_ids' => array($token),
                        'notification'     => $message,
        				'data'             => $data,
                    );
                    $this->useCurl($url, $headers, json_encode($fields));
                }
                if($not_code != 0){
                    $this->add_push_relation($token,$lastid);
                }
            }

            /**
             * Start : Send-Notification for test notification Api
             */
            if($not_code == 0){
                $fields = array(
                    'registration_ids' => array($token),
                    'notification'     => $message,
    				'data'             => $data,
                );
                $this->useCurl($url, $headers, json_encode($fields));
            }
            /** End */
        }
        return true;
    }

    // Curl
	private function useCurl( $url, $headers, $fields = null) {


            // Open connection
	        $ch = curl_init();
	        if ($url) {
	            // Set the url, number of POST vars, POST data
	            $result = '';
                curl_setopt($ch, CURLOPT_URL, $url);
	            curl_setopt($ch, CURLOPT_POST, true);
	            curl_setopt($ch, CURLOPT_HTTPHEADER, $headers);
	            curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);

	            // Disabling SSL Certificate support temporarly
	            curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
	            if ($fields) {
	                curl_setopt($ch, CURLOPT_POSTFIELDS, $fields);
	            }

	            // Execute post
	            $result = curl_exec($ch);
	            if ($result === FALSE) {
	                //die('Curl failed: ' . curl_error($ch));
	            }

                // Close connection
	            curl_close($ch);

	            return $result;
        }
    }

    public function is_notifiction_on_to_token($token){
        global $wpdb;
        $push_table = $wpdb->prefix . $this->push_table;
        $status = 0;
        $qur = "SELECT status FROM $push_table WHERE device_token = '$token'";
        $results = $wpdb->get_row( $qur, OBJECT );
        if(isset($results->status)){
            $status = $results->status;
        }
        return $status;
    }

    //Add notification meta for notification log
    public function add_notification_meta($msg,$custom_msg,$not_code){
            global $wpdb;
            $push_meta_table = $wpdb->prefix . $this->push_meta_table;
            $metadata = array(
                'msg' => sanitize_text_field($msg),
                'custom_msg' => sanitize_text_field($custom_msg),
                'not_code' => sanitize_text_field($not_code),
                'created' => date("Y-m-d H:i:s")
            );
            $metaformate = array('%s','%s','%d','%s');
            $wpdb->insert( $push_meta_table,$metadata,$metaformate );
            $lastid = $wpdb->insert_id;
            return $lastid;
    }

    //Add push_relation for multiple divice token
    public function add_push_relation($token,$lastid){
        global $wpdb;
        $push_table = $wpdb->prefix . $this->push_table;
        $push_relation_table = $wpdb->prefix . $this->push_relation_table;

        $qur = "SELECT * FROM $push_table WHERE device_token = '$token'";
        $results = $wpdb->get_results( $qur, OBJECT );

        if(!empty($results)){

            foreach($results as $result){
                $data = array(
                    'not_id' => $result->id,
                    'user_id' => $result->user_id,
                    'push_meta_id' => $lastid,

                );
                $formate = array('%d','%d','%d');
                $wpdb->insert( $push_relation_table,$data,$formate );
            }
        }
    }

    public function filter_array_ios_arr($value){
        return ($value['type'] == 1);
    }

    private function filter_array_android($value){
        return ($value['type'] == 2);
    }


    /**
    * Vendor plugin Short info for product listing page
    */
    public function pgs_woo_api_get_seller_short_details($product_id){

        $data = array(
            'is_seller' => false
        );
        $author_id  = get_post_field( 'post_author', $product_id );

        $is_vendeo = pgs_woo_api_is_vendor_plugin_active();
        //echo '==>'.$is_vendeo['vendor_for'];
        if($is_vendeo['vendor_count'] > 0){
            if($is_vendeo['vendor_for'] == 'dokan'){
                $data = $this->get_dokan_vender_short_info($author_id);
			} else if($is_vendeo['vendor_for'] == 'wc_marketplace'){
				$data = $this->get_wc_marketplace_vender_short_info($author_id,$product_id);
            } else {
				$data = $this->get_wcfm_vender_short_info($author_id,$product_id);
            }
        }
        return $data;
    }

    /**
    * Dokan plugin vendor Short info for product listing page
    */
    public function get_dokan_vender_short_info($author_id){
        $info       = get_user_meta( $author_id, 'dokan_profile_settings', true );
        $data = array();
        if(isset($info) && !empty($info)){
            $author     = get_user_by( 'id', $author_id );
            $store_info = dokan_get_store_info( $author->ID );
            $seller_address = dokan_get_seller_address( $author->ID );
            $seller_rating = dokan_get_seller_rating( $author->ID );
            $store_tnc = (isset($store_info['store_tnc']))?nl2br($store_info['store_tnc']):'';

            //check support status on off
            $contact_seller = $this->pgs_woo_api_contact_seller_status('dokan');
            if( ( isset( $seller_rating['count'] ) && $seller_rating['count'] === 0 ) || (isset($seller_rating['rating']) && $seller_rating['rating'] == 'No Ratings found yet' ) ){
                $seller_rating = array(
                    "rating"=>"0.00",
                    "count"=>0
                );
            }

            $data = array(
                'is_seller' => true,
                'seller_id' => $author_id,
                'store_name' => $store_info['store_name'],
                'address' => $store_info['address'],
                'seller_address' => $seller_address,
                'seller_rating' => $seller_rating,
                'store_tnc' => $store_tnc,
                'contact_seller' => $contact_seller,
				'sold_by' => true
            );
        } else {
            $data = array(
                'is_seller' => false,
				'sold_by' => true
            );
        }
        return $data;
    }

    /**
    * WC Marketplace plugin vendor Short info for product listing page
    */
    public function get_wc_marketplace_vender_short_info($author_id,$product_id){
		global $WCMp;
        $html = '';$data = array();
        $vendor = get_wcmp_vendor($author_id);
        if ($vendor) {

            $term_vendor = wp_get_post_terms($product_id, 'dc_vendor_shop');
            $contact_seller = $this->pgs_woo_api_contact_seller_status('wc_marketplace');

            if (!is_wp_error($term_vendor) && !empty($term_vendor)) {

                $rating_result_array = wcmp_get_vendor_review_info($term_vendor[0]->term_id);

				$store_tnc = '';

				// Check whether policies is enabled.
				if( get_wcmp_vendor_settings('is_policy_on', 'general') == 'Enable' ) {
					$policies_info = $this->get_wc_marketplace_policies_info( $author_id, $product_id );
					$store_tnc = ( isset( $policies_info ) && ! empty( $policies_info ) ) ? $policies_info : '';
				}

				$sold_by_catalogg = get_option('wcmp_general_settings_name');
				$sold_by_catalogg = $sold_by_catalogg['sold_by_catalog'];

				$seller_rating = array(
                    "rating" => $rating_result_array['avg_rating'],
                    "count" => $rating_result_array['total_rating']
				);

                $data = array(
					'is_seller'     => true,
					'seller_id'     => $author_id,
					'store_name'    => $vendor->user_data->display_name,
					'address'       => '',
					'seller_address'=> '',
					'seller_rating' => $seller_rating,
					'store_tnc'     => $store_tnc,
					'contact_seller'=> $contact_seller,
					'sold_by'       => ($sold_by_catalogg) ? true : false
                );

            }
        } else {
            $data = array(
				'is_seller' => false,
				'sold_by' => false
            );
        }

        return $data;
    }

	/**
	 * WCFM plugin vendor Short info for product listing page
	 */
	public function get_wcfm_vender_short_info( $vendor_id, $product_id ) {
		global $WCFM, $WCFMmp;

		$data                  = array();
		$store_user            = wcfmmp_get_store( $vendor_id );
		$store_tabs            = $store_user->get_store_tabs();
		$disable_vendor        = get_user_meta( $vendor_id, '_disable_vendor', true );
		$store_name            = $WCFM->wcfm_vendor_support->wcfm_get_vendor_store_name_by_vendor( absint( $vendor_id ) );

		if ( apply_filters( 'wcfm_is_pref_vendor_reviews', true ) ) {
			$seller_rating_avg     = $WCFMmp->wcfmmp_reviews->get_vendor_review_rating( absint( $vendor_id ) );
			$seller_rating_count   = $WCFMmp->wcfmmp_reviews->get_vendor_reviews_count( absint( $vendor_id ) );
		}
		$seller_rating         = array(
			"rating" => ( isset( $seller_rating_avg ) && ! empty( $seller_rating_avg ) ) ? $seller_rating_avg : 0,
			"count"  => ( isset( $seller_rating_avg ) && ! empty( $seller_rating_avg ) ) ? ( ( isset( $seller_rating_count ) && ! empty( $seller_rating_count ) ) ? $seller_rating_count : 0 ) : 0,
		);

		$store_tnc             = '';
		if ( isset( $store_tabs['policies'] ) ) {
			$store_policies        = $store_user->get_store_policies();
			$wcfm_policy           = wcfm_get_option( 'wcfm_policy_options', array() );

			$shipping_policy       = isset( $store_policies['shipping_policy'] ) ? $store_policies['shipping_policy'] : '';
			$_wcfm_shipping_policy = isset( $wcfm_policy['shipping_policy'] )    ? $wcfm_policy['shipping_policy']    : '';

			if( wcfm_empty($shipping_policy) ) $shipping_policy = $_wcfm_shipping_policy;

			$refund_policy         = isset( $store_policies['refund_policy'] ) ? $store_policies['refund_policy'] : '';
			$_wcfm_refund_policy   = isset( $wcfm_policy['refund_policy'] )    ? $wcfm_policy['refund_policy']    : '';
			if( wcfm_empty($refund_policy) ) $refund_policy = $_wcfm_refund_policy;

			$cancellation_policy       = isset( $store_policies['cancellation_policy'] ) ? $store_policies['cancellation_policy'] : '';
			$_wcfm_cancellation_policy = isset( $wcfm_policy['cancellation_policy'] )    ? $wcfm_policy['cancellation_policy']    : '';
			if( wcfm_empty($cancellation_policy) ) $cancellation_policy = $_wcfm_cancellation_policy;

			if( ! wcfm_empty($shipping_policy) ) {
				$store_tnc .= '<h2 class="wcfm_policies_heading">' . apply_filters('wcfm_shipping_policies_heading', __('Shipping Policy', 'pgs-woo-api')) . '</h2>';
				$store_tnc .= '<div class="wcfm_policies_description" >' . $shipping_policy . '</div>';
			}
			if( ! wcfm_empty( $refund_policy ) ) {
				$store_tnc .= '<h2 class="wcfm_policies_heading">' . apply_filters('wcfm_refund_policies_heading', __('Refund Policy', 'pgs-woo-api')) . '</h2>';
				$store_tnc .= '<div class="wcfm_policies_description" >' . $refund_policy . '</div>';
			}
			if( ! wcfm_empty( $cancellation_policy ) ) {
				$store_tnc .= '<h2 class="wcfm_policies_heading">' . apply_filters('wcfm_cancellation_policies_heading', __('Cancellation / Return / Exchange Policy', 'pgs-woo-api')) . '</h2>';
				$store_tnc .= '<div class="wcfm_policies_description" >' . $cancellation_policy . '</div>';
			}
		}

		$contact_seller        = ( apply_filters( 'wcfm_is_pref_enquiry', true ) && apply_filters( 'wcfmmp_is_allow_store_header_enquiry', true ) && $WCFM->wcfm_vendor_support->wcfm_vendor_has_capability( $store_user->get_id(), 'enquiry' ) );
		$sold_by               = ( apply_filters( 'wcfmmp_is_allow_sold_by', true, $vendor_id ) && $WCFM->wcfm_vendor_support->wcfm_vendor_has_capability( $vendor_id, 'sold_by' ) ) && ( $WCFMmp->wcfmmp_vendor->is_vendor_sold_by() );

		if ( ! $disable_vendor ) {

			$user_obj     = get_user_by( 'ID', absint( $vendor_id ) );

			if ( in_array('wcfm_vendor', $user_obj->roles ) ) {

                $data = array(
                    'is_seller'         => true,
                    'seller_id'         => $vendor_id,
                    'store_name'        => $store_name,
                    'address'           => '',
                    'seller_address'    => $store_user->get_address_string(),
                    'seller_rating'     => $seller_rating,
                    'store_tnc'         => $store_tnc,
                    'contact_seller'    => $contact_seller,
                    'sold_by'           => $sold_by,
                );
            } else {
                $data = array(
                    'is_seller' => false,
                    'sold_by' => false
                );
            }
		} else {
			$data = array(
				'is_seller' => false,
				'sold_by' => false
			);
		}

		return $data;
	}

    /**
    * Get wc marketplace policies info
    */
    public function get_wc_marketplace_policies_info( $author_id, $product_id = null ) {

		$cancellation_policy_label  = __('Cancellation/Return/Exchange Policy','pgs-woo-api');
		$refund_policy_label        = __('Refund Policy','pgs-woo-api');
		$shipping_policy_label      = __('Shipping Policy','pgs-woo-api');

		$wcmp_policy_settings = get_option( 'wcmp_general_policies_settings_name' );

		$shipping_policy     = get_wcmp_vendor_settings('shipping_policy');
		$refund_policy       = get_wcmp_vendor_settings('refund_policy');
		$cancellation_policy = get_wcmp_vendor_settings('cancellation_policy');

		if ( apply_filters( 'wcmp_vendor_can_overwrite_policies', true ) ) {
			$shipping_policy     = get_user_meta( $author_id, '_vendor_shipping_policy', true)    ? get_user_meta( $author_id, '_vendor_shipping_policy', true)     : $shipping_policy;
			$refund_policy       = get_user_meta( $author_id, '_vendor_refund_policy', true)      ? get_user_meta( $author_id, '_vendor_refund_policy', true)       : $refund_policy;
			$cancellation_policy = get_user_meta( $author_id, '_vendor_cancellation_policy', true)? get_user_meta( $author_id, '_vendor_cancellation_policy', true) : $cancellation_policy;
		}

		if( apply_filters( 'pgs_woo_api_wc_marketplace_product)level_policies', false ) ) {
			if ( get_post_meta( $product_id, '_wcmp_shipping_policy', true ) ) {
				$shipping_policy = get_post_meta( $product_id, '_wcmp_shipping_policy', true);
			}
			if ( get_post_meta( $product_id, '_wcmp_refund_policy', true ) ) {
				$refund_policy = get_post_meta( $product_id, '_wcmp_refund_policy', true);
			}
			if ( get_post_meta( $product_id, '_wcmp_cancallation_policy', true ) ) {
				$cancellation_policy = get_post_meta( $product_id, '_wcmp_cancallation_policy', true);
			}
		}

		$html = '';

		if ( ! empty( $shipping_policy ) ) {
        	$html .= '<h2 class="wcmp_policies_heading">'.$shipping_policy_label.'</h2>';
        	$html .= '<div class="wcmp_policies_description">'.$shipping_policy.'</div>';
        }

        if ( ! empty( $refund_policy ) ) {
        	$html .= '<h2 class="wcmp_policies_heading">'.$refund_policy_label.'</h2>';
        	$html .= '<div class="wcmp_policies_description">'.$refund_policy.'</div>';
        }

		if ( ! empty( $cancellation_policy ) ) {
        	$html .= '<h2 class="wcmp_policies_heading">'.$cancellation_policy_label.'</h2>';
        	$html .= '<div class="wcmp_policies_description" >'.$cancellation_policy.'</div>';
        }

        return $html;
    }

    /**
     * Check contact seller status for active or not
     */
    public function pgs_woo_api_contact_seller_status($seller_for){
        $contact_seller = false;
		if( $seller_for == 'dokan'){
            if( dokan_get_option( 'contact_seller', 'dokan_general', 'on' ) == 'on' ) {
                $contact_seller = true;
            }
        } else {
            $contact_seller = false;
            $capability_settings = get_option('wcmp_general_customer_support_details_settings_name');
			if( isset( $capability_settings['can_vendor_add_customer_support_details'] ) ) {
				$vendor_meta = get_user_meta( $vendor_id );
				if( isset($vendor_meta['_vendor_customer_email'][0])) {
                    if(isset($vendor_meta['_vendor_customer_email'][0])) {
                        $to_email = $vendor_meta['_vendor_customer_email'][0];
                    }
				}
    		} else {
                if(isset($capability_settings['csd_email'])) {
                    $to_email = $capability_settings['csd_email'];
                }
    		}
            if( isset($to_email) && !empty($to_email) ){
                $contact_seller = true;
            }
        }
        return $contact_seller;
    }

    public function get_upload_image_data($image_data,$user_id){

        $img = strtotime(date('Ymdhis'));
        $file_name = $image_data['name'];
        $ext = pathinfo($file_name,PATHINFO_EXTENSION);
        $type = array("jpge","jpg","png");
        if(in_array($ext,$type)) {
            //$destination = trailingslashit( PGS_API_PATH . 'img/profile_img' ) . 'user_'.$user_id.'_'.$img.'.'.$ext;
            $upload = wp_upload_dir();
            $profile_img_dir_path = $upload['basedir'].'/pgs-woo-api/profile_img';
            // Create profile_img directory
            if (!is_dir($profile_img_dir_path)) {
                wp_mkdir_p( $profile_img_dir_path );
            }
            $profile_img_dir_url = $upload['baseurl'].'/pgs-woo-api/profile_img';
            $destination = trailingslashit( $profile_img_dir_path ) . 'user_'.$user_id.'_'.$img.'.'.$ext;
            file_put_contents($destination,base64_decode($image_data['data']));
            $img_url = trailingslashit( $profile_img_dir_url ) . 'user_'.$user_id.'_'.$img.'.'.$ext;
            update_user_meta( $user_id, 'pgs_user_image', $img_url );
            return true;
        } else {
            return true;
        }
    }

    public function pgs_woo_api_get_yith_featured_video($product,$product_id){
        $args = array();
        $video_url = yit_get_prop( $product, '_video_url' );
		if ( ! empty( $video_url ) ) {

			$result             = true;
			$video_host         = parse_url( esc_url( $video_url ) );
			$args['url']        = $video_url;
			$args['product_id'] = $product_id;
            //$YITH_Featured_Audio_Video = YITH_WC_Audio_Video::get_instance();
			//$src =  $atts['url'];
            $http = is_ssl() ? 'https' : 'http';
            list( $video_type, $video_id ) = explode( ':', ywcfav_video_type_by_url( $video_url ) );
            $args['video_type'] = $video_type;
            $args['video_id'] = $video_id;
            //echo $video_host['host'];
            /*global $YITH_Featured_Audio_Video;
            echo '<pre>';
            print_r($YITH_Featured_Audio_Video->hosts['youtube']);
            echo '</pre>';*/

            $image_url = '';
            //$image_url = $this->pgs_woo_api_get_yith_video_image_thmb($video_id,'youtube');
            if ( $video_type == 'youtube') {
                //$embed_url = $http.'://www.youtube.com/embed/'.$video_id.'?enablejsapi=1';
                $image_url = $this->pgs_woo_api_get_yith_video_image_thmb($video_id,'youtube');
			} elseif ( $video_type == 'vimeo' ) {
                //$embed_url = $http.'://www.youtube.com/embed/'.$video_id.'?enablejsapi=1';
                //$video_meta_id = "video_".$product_id;
                //$embed_url =  $http.'://player.vimeo.com/video/'.$video_id.'?api=1&player_id='.$video_meta_id;
                $image_url = $this->pgs_woo_api_get_yith_video_image_thmb($video_id,'vimeo');
			}
            //$args['embed_url'] = $embed_url;
            $args['image_url'] = $image_url;
        } else {
            $args = (object)array();
        }
        return $args;
    }

    public function pgs_woo_api_get_yith_video_image_thmb($video_id='',$host) {
		$result  = false;
		$img_url = '';
		if ( !empty($video_id) && !empty($host) ) {
			//$video_id = $video_info[0];
			//$host     = $video_info[1];
			switch ( $host ) {

				case 'vimeo' :
					$img_url = 'http://vimeo.com/api/v2/video/' . $video_id . '.xml';
					$xml     = simplexml_load_file( $img_url );
					$img_url = (string) $xml->video->thumbnail_large;
					$tmp     = getimagesize( $img_url );

					$result = ! is_wp_error( $tmp );

					break;
				case 'youtube':
					$img_url      = 'https://img.youtube.com/vi/' . $video_id . '/maxresdefault.jpg';
					$get_response = wp_remote_get( $img_url );
					$result       = ! is_wp_error( $get_response ) && $get_response['response']['code'] == '200';

					break;
			}
		}
		if ( ! $result ) {
			$img_url = YWCFAV_ASSETS_URL . '/images/videoplaceholder.jpg';
		}
		return $img_url;
	}

	/**
	 * Check whether usr exists.
	 *
	 * @param int             $user_id User ID.
	 * @param array           $params Array of params.
	 * @param WP_REST_Request $wp_rest_request Request data.
	 *
	 * @return bool
	 */
	public function is_user_exists( $user_id, $params, $wp_rest_request ) {
		$user   = get_user_by( 'id', absint( $user_id ) );
		$status = ( $user ) ? 'true' : 'false';

		return $status;
	}
}
