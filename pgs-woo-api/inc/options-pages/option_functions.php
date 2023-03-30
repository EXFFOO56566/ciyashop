<?php
/**
 * Save option page data
 */
if( isset($_POST['submit-api']) ){

    $lang='';
    $return = ( class_exists('SitePress') ? true : false );
    if($return){
        if(isset($_GET['lang']) && !empty($_GET['lang'])){
            $pgs_woo_api_wpml_initial_language = get_option('pgs_woo_api_wpml_initial_language_array');
            if(isset($pgs_woo_api_wpml_initial_language) && !empty($pgs_woo_api_wpml_initial_language)){
                $default_lang = $pgs_woo_api_wpml_initial_language['code'];
                if($_GET['lang'] != $default_lang){
                    $lang = $_GET['lang'];
                }
            }
        } else {
            $current_lang = apply_filters( 'wpml_current_language', NULL );
            if(isset($current_lang) && !empty($current_lang)){
                $pgs_woo_api_wpml_initial_language = get_option('pgs_woo_api_wpml_initial_language_array');
                if(isset($pgs_woo_api_wpml_initial_language) && !empty($pgs_woo_api_wpml_initial_language)){
                    $default_lang = $pgs_woo_api_wpml_initial_language['code'];
                    if($current_lang != $default_lang){
                        $lang = $current_lang;
                    } else {
                        $lang = '';
                    }
                }
            }
        }
    }

    if($lang == ''){
        if(isset($_POST['pgs']['main_category'])){
            foreach($_POST['pgs']['main_category'] as $k => $v){
                if ( isset( $v['main_cat_id'] ) && !empty($v['main_cat_id']) ) {
            		$product_app_cat_thumbnail_id = isset( $v['product_app_cat_thumbnail_id'] ) ? $v['product_app_cat_thumbnail_id'] : '';
                    update_term_meta($v['main_cat_id'], 'product_app_cat_thumbnail_id', $product_app_cat_thumbnail_id);
                }
            }
        }

        if(isset($_POST['pgs']['main_slider'])){
            $t = 0;
            foreach($_POST['pgs']['main_slider'] as $k => $v){
                if(isset($v['upload_image_id']) && !empty($v['upload_image_id']) ){
                    $vsrc = wp_get_attachment_image_src($v['upload_image_id'], 'large' );
                    if(!empty($vsrc)){
                        $_POST['pgs']['main_slider'][$t]['upload_image_url'] = esc_url($vsrc[0]);
                    } else {
                        $_POST['pgs']['main_slider'][$t]['upload_image_url'] = '';
                    }
                }
                $t++;
            }
        }

        if(isset($_POST['pgs']['category_banners'])){
            $p = 0;
            foreach($_POST['pgs']['category_banners'] as $k => $v){
                if(isset($v['cat_banners_image_id']) && !empty($v['cat_banners_image_id']) ){
                    $vsrc = wp_get_attachment_image_src($v['cat_banners_image_id'], 'app_thumbnail' );
                    if(!empty($vsrc)){
                        $_POST['pgs']['category_banners'][$p]['cat_banners_image_url'] = esc_url($vsrc[0]);
                    } else {
                        $_POST['pgs']['category_banners'][$p]['cat_banners_image_url'] = '';
                    }
                }

                if(isset($v['cat_banners_title']) && !empty($v['cat_banners_title']) ){
                    $_POST['pgs']['category_banners'][$p]['cat_banners_title'] = stripslashes($v['cat_banners_title']);
                }

                $p++;
            }
        }

        if(isset($_POST['pgs']['banner_ad'])){
            $p = 0;
            foreach($_POST['pgs']['banner_ad'] as $k => $v){
                if(isset($v['banner_ad_image_id']) && !empty($v['banner_ad_image_id']) ){
                    $banner_ad_image_id = $v['banner_ad_image_id'];
                    $vsrc = wp_get_attachment_image_src($banner_ad_image_id, 'large' );
                    if(!empty($vsrc)){
                        $_POST['pgs']['banner_ad'][$p]['banner_ad_image_url'] = $vsrc[0];
                    } else {
                        $_POST['pgs']['banner_ad'][$p]['banner_ad_image_url'] = '';
                    }
                }
                $p++;
            }
        }

        if(isset($_POST['pgs']['feature_box_heading'])){
            $_POST['pgs']['feature_box_heading'] = stripslashes($_POST['pgs']['feature_box_heading']);
        }
        if(isset($_POST['pgs']['feature_box'])){
            $p = 0;
            foreach($_POST['pgs']['feature_box'] as $k => $v){
                if(isset($v['feature_title']) && !empty($v['feature_title']) ){
                    $_POST['pgs']['feature_box'][$p]['feature_title'] = stripslashes($v['feature_title']);
                }
                if(isset($v['feature_content']) && !empty($v['feature_content']) ){
                    $_POST['pgs']['feature_box'][$p]['feature_content'] = stripslashes($v['feature_content']);
                }
                $p++;
            }
        }
        if(isset($_POST['pgs_woo_api_checkout_multistep'])){
            update_option('pgs_woo_api_checkout_multistep',$_POST['pgs_woo_api_checkout_multistep']);
            update_option('pgs_woo_api_multi_num_of_steps',$_POST['pgs_woo_api_multi_num_of_steps']);

        }
        if(isset($_POST['pgs_checkout_page'])){
            update_option('pgs_checkout_page', $_POST['pgs_checkout_page']);
        }
        if(isset($_POST['pgs_woo_api_checkout_custom_css'])){
            update_option('pgs_woo_api_checkout_custom_css',$_POST['pgs_woo_api_checkout_custom_css']);
        }
        //Payment Gateway redirect URLs Start
        if(isset($_POST['pgs_woo_api_checkout_custom_redirect_urls'])){
            $string_url=sanitize_textarea_field($_POST['pgs_woo_api_checkout_custom_redirect_urls']);
            update_option('pgs_woo_api_checkout_custom_redirect_urls',$string_url);
        }

        if(isset($_POST['pgs']['pgs_app_contact_info']['whatsapp_no'])){
            if(!empty($_POST['pgs']['pgs_app_contact_info']['whatsapp_no'])){
                $whatsapp_no = $_POST['pgs']['pgs_app_contact_info']['whatsapp_no'];
                $whatsapp_no = preg_replace('/\s+/', '', $whatsapp_no);
                $_POST['pgs']['pgs_app_contact_info']['whatsapp_no'] = sanitize_text_field($whatsapp_no);
            }
        }

        if(isset($_POST['pgs']['pgs_app_contact_info']['whatsapp_floating_button'])){
            if(empty($_POST['pgs']['pgs_app_contact_info']['whatsapp_no'])){
                $_POST['pgs']['pgs_app_contact_info']['whatsapp_floating_button'] = 'disable';
            }
        }

        //Payment Gateway redirect URLs Ends
		$post_data = pgs_woo_api_clean( wp_unslash( $_POST['pgs'] ) );
        update_option( 'pgs_woo_api_home_option', $post_data );
        $app_assets = array();

        /**
         * App color option for home api
         */
        $app_assets['app_assets']['app_color']['primary_color'] = '#60A727';
        $app_assets['app_assets']['app_color']['secondary_color'] = '';
        if(isset($_POST['pgs_app_assets']['app_color'])){
            $app_assets['app_assets']['app_color'] = $_POST['pgs_app_assets']['app_color'];
        }
        $data = array (
            'header_color' => '#1e73be',
            'primary_color' => '#1e73be',
            'secondary_color' => '#8224e3'
        );
        update_option( 'pgs_woo_api_app_assets_options',$app_assets );

		if ( isset( $_POST['_pgs_woo_api_woowallet_topup_page'] ) ) {
            update_option( '_pgs_woo_api_woowallet_topup_page', $_POST['_pgs_woo_api_woowallet_topup_page'] );
        }

    } else {

        if(isset($_POST['pgs']['app_logo_light'])){
            $_POST['pgs'.$lang]['app_logo_light'] = stripslashes($_POST['pgs']['app_logo_light']);
        }

        if(isset($_POST['pgs']['app_logo'])){
            $_POST['pgs'.$lang]['app_logo'] = stripslashes($_POST['pgs']['app_logo']);
        }

        if(isset($_POST['pgs']['main_slider'])){
            $t = 0;
            foreach($_POST['pgs']['main_slider'] as $k => $v){
                $_POST['pgs'.$lang]['main_slider'][$t]['upload_image_id'] = $v['upload_image_id'];
                if(isset($v['upload_image_id']) && !empty($v['upload_image_id']) ){
                    $vsrc = wp_get_attachment_image_src($v['upload_image_id'], 'large' );
                    if(!empty($vsrc)){
                        $_POST['pgs'.$lang]['main_slider'][$t]['upload_image_url'] = esc_url($vsrc[0]);
                    } else {
                        $_POST['pgs'.$lang]['main_slider'][$t]['upload_image_url'] = '';
                    }
                }

                if(isset($v['slider_cat_id']) && !empty($v['slider_cat_id']) ){

                    if(!empty($v['slider_cat_id'])){
                        $_POST['pgs'.$lang]['main_slider'][$t]['slider_cat_id'] = $v['slider_cat_id'];
                    } else {
                        $_POST['pgs'.$lang]['main_slider'][$t]['slider_cat_id'] = '';
                    }
                }
                $t++;
            }
        }


        if(isset($_POST['pgs']['category_banners'])){
            $p = 0;
            foreach($_POST['pgs']['category_banners'] as $k => $v){
                $_POST['pgs'.$lang]['category_banners'][$p]['cat_banners_image_id'] = $v['cat_banners_image_id'];
                if(isset($v['cat_banners_image_id']) && !empty($v['cat_banners_image_id']) ){
                    $vsrc = wp_get_attachment_image_src($v['cat_banners_image_id'], 'app_thumbnail' );
                    if(!empty($vsrc)){
                        $_POST['pgs'.$lang]['category_banners'][$p]['cat_banners_image_url'] = esc_url($vsrc[0]);
                    } else {
                        $_POST['pgs'.$lang]['category_banners'][$p]['cat_banners_image_url'] = '';
                    }
                }

                if(isset($v['cat_banners_title']) && !empty($v['cat_banners_title']) ){
                    $_POST['pgs'.$lang]['category_banners'][$p]['cat_banners_title'] = stripslashes($v['cat_banners_title']);
                }

                if(isset($v['cat_banners_cat_id']) && !empty($v['cat_banners_cat_id']) ){
                    $_POST['pgs'.$lang]['category_banners'][$p]['cat_banners_cat_id'] = stripslashes($v['cat_banners_cat_id']);
                } else {
                    $_POST['pgs'.$lang]['category_banners'][$p]['cat_banners_cat_id'] = '';
                }
                $p++;
            }
        }


        if(isset($_POST['pgs']['banner_ad'])){
            $p = 0;
            foreach($_POST['pgs']['banner_ad'] as $k => $v){
                $_POST['pgs'.$lang]['banner_ad'][$p]['banner_ad_image_id'] = $v['banner_ad_image_id'];
                if(isset($v['banner_ad_image_id']) && !empty($v['banner_ad_image_id']) ){
                    $banner_ad_image_id = $v['banner_ad_image_id'];
                    $vsrc = wp_get_attachment_image_src($banner_ad_image_id, 'large' );
                    if(!empty($vsrc)){
                        $_POST['pgs'.$lang]['banner_ad'][$p]['banner_ad_image_url'] = $vsrc[0];
                    } else {
                        $_POST['pgs'.$lang]['banner_ad'][$p]['banner_ad_image_url'] = '';
                    }
                }

                if(isset($v['banner_ad_cat_id']) && !empty($v['banner_ad_cat_id']) ){
                    if(!empty($v['banner_ad_cat_id'])){
                        $_POST['pgs'.$lang]['banner_ad'][$p]['banner_ad_cat_id'] = $v['banner_ad_cat_id'];
                    } else {
                        $_POST['pgs'.$lang]['banner_ad'][$p]['banner_ad_cat_id'] = '';
                    }
                }
                $p++;
            }
        }

        if(isset($_POST['pgs']['feature_box_status'])){
            $_POST['pgs'.$lang]['feature_box_status'] = $_POST['pgs']['feature_box_status'];
        }
        if(isset($_POST['pgs']['feature_box_heading'])){
            $_POST['pgs'.$lang]['feature_box_heading'] = stripslashes($_POST['pgs']['feature_box_heading']);
        }

        if(isset($_POST['pgs']['feature_box'])){
            $p = 0;
            foreach($_POST['pgs']['feature_box'] as $k => $v){
                $_POST['pgs'.$lang]['feature_box'][$p]['feature_image_id'] = $v['feature_image_id'];
                if(isset($v['feature_title'])){
                    $_POST['pgs'.$lang]['feature_box'][$p]['feature_title'] = stripslashes($v['feature_title']);
                }
                if(isset($v['feature_content'])){
                    $_POST['pgs'.$lang]['feature_box'][$p]['feature_content'] = stripslashes($v['feature_content']);
                }
                $p++;
            }
        }

        if(isset($_POST['pgs']['products_carousel'])){
            $products_carousel_default = array(
                'feature_products' => array(
                    'status' => "enable",
                    'title' => "Feature Products"
                ),
                'recent_products' => array(
                    'status' => "enable",
                    'title' => ""
                ),
                'special_deal_products' => array(
                    'status' => "enable",
                    'title' => "Special Deal"
                ),
                'popular_products' => array(
                    'status' => "enable",
                    'title' => "Popular Products"
                ),
                'top_rated_products' => array(
                    'status' => "enable",
                    'title' => "Top Rated products"
                )
            );

            foreach($_POST['pgs']['products_carousel'] as $key => $val){
                $status = (isset($_POST['pgs']['products_carousel'][$key]['status']))?$_POST['pgs']['products_carousel'][$key]['status']:$products_carousel_default[$key]['status'];
                $title = (isset($_POST['pgs']['products_carousel'][$key]['title']))?$_POST['pgs']['products_carousel'][$key]['title']:$products_carousel_default[$key]['title'];
                foreach($_POST['pgs']['products_carousel'] as $key => $val){
                    $status = (isset($_POST['pgs']['products_carousel'][$key]['status']))?$_POST['pgs']['products_carousel'][$key]['status']:$products_carousel_default[$key]['status'];
                    $title = (isset($_POST['pgs']['products_carousel'][$key]['title']))?$_POST['pgs']['products_carousel'][$key]['title']:$products_carousel_default[$key]['title'];
                    $_POST['pgs'.$lang]['products_carousel'][$key]['status'] = $status;
                    $_POST['pgs'.$lang]['products_carousel'][$key]['title'] = $title;
                }
            }
        }
        update_option('pgs_woo_api_home_option_'.$lang,$_POST['pgs'.$lang]);
    }
    $message = esc_html__( 'Settings saved.', 'pgs-woo-api' );
    echo pgs_woo_api_admin_notice_render($message,'success');
}

/**
 * Use for upload pen file for notification
 */
function pgs_pem_upload($file_name,$source){

    $ext = pathinfo($file_name,PATHINFO_EXTENSION);
    $responce = array();
    if($ext == "pem") {
        //$destination = trailingslashit( PGS_API_PATH . 'inc/options-pages/pem' ) . $file_name;
        $upload = wp_upload_dir();
	    $pem_dir_path = $upload['basedir'].'/pgs-woo-api/pem';
        // Create pem file directory
        if (!is_dir($pem_dir_path)) {
            wp_mkdir_p( $pem_dir_path );
        }
        $destination = trailingslashit( $pem_dir_path ) . $file_name;
        if (move_uploaded_file( $source, $destination )) {
            $responce = array(
                'status' => 'success',
                'message' => esc_html__( "The file ",'pgs-woo-api' ). basename( $file_name). esc_html__( " has been uploaded.",'pgs-woo-api' )
            );

        } else {
            $responce = array(
                'status' => 'error',
                'message' => esc_html__("Sorry, there was an error uploading your file.",'pgs-woo-api' )
            );
        }
    } else {
        $responce = array(
            'status' => 'error',
            'message' => esc_html__("Sorry, there was an error uploading your file.",'pgs-woo-api')
        );
    }
    return $responce;
}
/**
 * Update Setting page daga
 */
if( isset($_POST['submit-api-auth']) ){

    foreach($_POST as $key => $val ){

        if($key == "pgs_auth"){
			$pgs_auth['pgs_auth']['client_key']    = sanitize_text_field($val['client_key']);
			$pgs_auth['pgs_auth']['client_secret'] = sanitize_text_field($val['client_secret']);
			$token                                 = (isset($val['token']))?$val['token']:'';
			$token_secret                          = (isset($val['token_secret']))?$val['token_secret']:'';
			$pgs_auth['pgs_auth']['token']         = sanitize_text_field($token);
			$pgs_auth['pgs_auth']['token_secret']  = sanitize_text_field($token_secret);
        }

        if($key == "woo_auth"){
            $pgs_auth['woo_auth']['client_key']    = sanitize_text_field($val['client_key']);
            $pgs_auth['woo_auth']['client_secret'] = sanitize_text_field($val['client_secret']);
        }

        if($key == "google_keys"){
            $google_keys['google_keys']['google_map_api_key'] = sanitize_text_field($val['google_map_api_key']);
            update_option('pgs_google_keys',$google_keys);
        }


        if($key == "push_mode"){
            update_option('pgs_push_mode', $val);
        }

        if($key == "push_status"){
            update_option('pgs_push_status', $val);
        }

        if($key == "pgs_not_code"){
            update_option('pgs_not_code', $val);
        }

        if($key == "android_l_s_key"){
            update_option('android_l_s_key', $val);
        }

        if($key == "pgs_ios_app_url"){
            update_option('pgs_ios_app_url', $val);
        }

        if($key == "active_vendor"){
            update_option('pgs_active_vendor', $val);
        }

        if($key == "pem_file_dev_pass"){
            update_option('pem_file_dev_pass', $val);
        }

        if($key == "pem_file_pro_pass"){
            update_option('pem_file_pro_pass', $val);
        }
    }


    if(isset($_FILES["pem_file_dev"]["name"]) && !empty($_FILES["pem_file_dev"]["name"])){
        $resutl = pgs_pem_upload($_FILES["pem_file_dev"]["name"],$_FILES["pem_file_dev"]["tmp_name"]);
        if( $resutl['status'] == 'success' ){
            $message = $resutl['message'];
            $status = $resutl['status'];
            echo pgs_woo_api_admin_notice_render($message,$status);
            update_option('pem_file_dev',$_FILES["pem_file_dev"]["name"]);
        } else {
            $message = $resutl['message'];
            $status = $resutl['status'];
            echo pgs_woo_api_admin_notice_render($message,$status);
        }
    }

    if(isset($_FILES["pem_file_pro"]["name"]) && !empty($_FILES["pem_file_pro"]["name"])){
        $resutl = pgs_pem_upload($_FILES["pem_file_pro"]["name"],$_FILES["pem_file_pro"]["tmp_name"]);
        if( $resutl['status'] == 'success' ){
            $message = $resutl['message'];
            $status = $resutl['status'];
            echo pgs_woo_api_admin_notice_render($message,$status);
            update_option('pem_file_pro',$_FILES["pem_file_pro"]["name"]);
        } else {
            $message = $resutl['message'];
            $status = $resutl['status'];
            echo pgs_woo_api_admin_notice_render($message,$status);
        }
    }
    update_option('app_auth',$pgs_auth);

    update_option('pgs_woo_api_emails_contact_recipient',$_POST['pgs_woo_api_emails_contact_recipient']);
    update_option('pgs_woo_api_emails_contact_from_name',$_POST['pgs_woo_api_emails_contact_from_name']);
    update_option('pgs_woo_api_emails_contact_from_address',$_POST['pgs_woo_api_emails_contact_from_address']);


    update_option('pgs_woo_api_emails_forgot_password_subject',$_POST['pgs_woo_api_emails_forgot_password_subject']);
    update_option('pgs_woo_api_emails_forgot_password_from_name',$_POST['pgs_woo_api_emails_forgot_password_from_name']);
    update_option('pgs_woo_api_emails_forgot_password_address',$_POST['pgs_woo_api_emails_forgot_password_address']);


    update_option('pgs_woo_api_emails_vendor_contact_subject',$_POST['pgs_woo_api_emails_vendor_contact_subject']);
    update_option('pgs_woo_api_emails_vendor_contact_from_name',$_POST['pgs_woo_api_emails_vendor_contact_from_name']);
    update_option('pgs_woo_api_emails_vendor_contact_address',$_POST['pgs_woo_api_emails_vendor_contact_address']);

    $message = esc_html__( 'Settings saved.', 'pgs-woo-api' );
    echo pgs_woo_api_admin_notice_render($message,'success');
}


function pgs_woo_api_get_app_cat_icon_url($id='',$echo=true){
    if(empty($id)){
        return false;
    }
    $vsrc = array();$app_cat='';
	$product_app_cat_thumbnail_id = get_term_meta($id, 'product_app_cat_thumbnail_id', true);
    if(isset($product_app_cat_thumbnail_id) && !empty($product_app_cat_thumbnail_id)){
        $vsrc = wp_get_attachment_image_src($product_app_cat_thumbnail_id, 'thumbnail' );
        if(!empty($vsrc)){
            if(!$echo){
                return esc_url($vsrc[0]);
            } else {
                echo esc_url($vsrc[0]);
            }
        }
    }

}

function pgs_woo_api_get_app_cat_icon_id($id='',$echo=true){
    if(empty($id)){
        return false;
    }
    $vsrc = array();$app_cat='';
	$product_app_cat_thumbnail_id = get_term_meta($id, 'product_app_cat_thumbnail_id', true);
    if(isset($product_app_cat_thumbnail_id) && !empty($product_app_cat_thumbnail_id)){
        if(!$echo){
            return $product_app_cat_thumbnail_id;
        } else {
            echo $product_app_cat_thumbnail_id;
        }
    }
}
