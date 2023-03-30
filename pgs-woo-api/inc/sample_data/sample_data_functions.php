<?php
// Prepapre Sample Data folder details
function pgs_woo_api_plugin_sample_datas(){
	return apply_filters( 'pgs_woo_api_plugin_sample_datas', array() );
}


add_filter( 'pgs_woo_api_plugin_sample_datas', 'pgs_woo_api_sample_data_items' );
function pgs_woo_api_sample_data_items( $sample_data = array() ){
	$sample_data_new = array (
		'default' => array (
			'id'         => 'default',
			'name'       => 'Default',
			'home_page'  => 'Home',
			'blog_page'  => 'Blog',
			'message'    => '',
			'preview_android'=> 'https://play.google.com/store/apps/details?id=com.potenza.ciyashop_cloths',
			'preview_ios'=> 'https://itunes.apple.com/us/app/ciyashop/id1291266157?mt=8',
            'preview_url'=> 'http://ciyashop.potenzaglobalsolutions.com/'
		),
		'electronics' => array (
			'id'         => 'electronics',
			'name'       => 'Electronics',
			'demo_url'   => 'http://ciyashop.potenzaglobalsolutions.com/electronics',
			'home_page'  => 'Home',
			'blog_page'  => 'Blog',
			'message'    => '',
			'preview_android' => 'https://play.google.com/store/apps/details?id=com.potenza.ciyashop_electronic',
            'preview_ios'=> 'https://itunes.apple.com/us/app/ciyashopelectronics/id1297927477?mt=8',
            'preview_url'=> 'http://themes.potenzaglobalsolutions.com/ciya-shop-electronics-wp/'
		),
		'jewellery' => array (
			'id'         => 'jewellery',
			'name'       => 'Jewellery',
			'demo_url'   => 'http://ciyashop.potenzaglobalsolutions.com/jewellery/',
			'home_page'  => 'Home',
			'blog_page'  => 'Blog',
			'message'    => '',
			'preview_android' => 'https://play.google.com/store/apps/details?id=com.potenza.ciyashop_jwellarys',
            'preview_ios'=> 'https://itunes.apple.com/us/app/ciyashopjewellery/id1304998874?mt=8',
            'preview_url'=> 'http://themes.potenzaglobalsolutions.com/ciya-shop-jewellery-wp/'
		),
		'onveggie' => array (
			'id'         => 'onveggie',
			'name'       => 'OnVeggie',
			'demo_url'   => 'http://ciyashop.potenzaglobalsolutions.com/onveggie/',
			'home_page'  => 'Home',
			'blog_page'  => 'Blog',
			'message'    => '',
			'preview_android' => 'https://play.google.com/store/apps/details?id=com.potenza.onveggy',
            'preview_ios'=> 'https://itunes.apple.com/us/app/onveggy/id1341266349?ls=1&mt=8',
            'preview_url'=> 'http://ciyashop.potenzaglobalsolutions.com/onveggie/'
		),
	);

	// $sample_data_new
	array_walk( $sample_data_new, 'pgs_woo_api_old_sample_data_fix' );

	$sample_data = array_merge( $sample_data, $sample_data_new );

	return $sample_data;
}


function pgs_woo_api_old_sample_data_fix(&$item1, $key){
	$sample_data_path = PGS_API_PATH.'includes/sample_data';
	$sample_data_url  = PGS_API_URL.'includes/sample_data';

	$item1['data_dir'] = trailingslashit(trailingslashit($sample_data_path).$key);
	$item1['data_url'] = trailingslashit(trailingslashit($sample_data_url).$key);
}



add_action( 'wp_ajax_pgs_woo_api_plugin_import_sample', 'pgs_woo_api_plugin_import_sample_data' );
function pgs_woo_api_plugin_import_sample_data(){
	global $pgs_woo_api_globals;

	sleep(0);

	$action_source = 'default';
	if( isset($_REQUEST['action_source']) && $_REQUEST['action_source'] == 'wizard' ){
		$action_source = 'wizard';
	}
	// First check the nonce, if it fails the function will break
	if ( ! wp_verify_nonce( $_REQUEST['sample_import_nonce'], 'pgs_woo_api_sample_data_security' ) ) {
		$import_status_data = array(
			'success'     => false,
			'message'     => esc_html__( 'Unable to validate security check. Please reload the page and try again.', 'pgs-woo-api' ),
			'action'      => ''
		);
	}else{

		// Nonce is checked, get the posted data and process further
		$sample_id = isset($_REQUEST['sample_id']) ? sanitize_text_field($_REQUEST['sample_id']) : '';

		if( empty($sample_id) ){
			$import_status_data = array(
				'success'     => false,
				'message'     => esc_html__('Something went wrong or invalid sample selected.','pgs-woo-api'),
			);
		}else{
			global $wpdb;

			if ( !current_user_can( 'manage_options' ) ) {
				$import_status_data = array(
					'success'     => false,
					'message'     => esc_html__('You are not allowed to perform this action.','pgs-woo-api'),
				);
			}else{
				    $sample_datas = pgs_woo_api_plugin_sample_datas();
					$sample_data =  $sample_datas[$sample_id];

					/******************************************
					 * Import Main Data
					 ******************************************/
					require_once(ABSPATH . 'wp-admin/includes/file.php');

                    flush_rewrite_rules();

					WP_Filesystem();
					global $wp_filesystem;

					/* -------------------------------------------------------
					 *
					 * Import pgs_woo_api Options
					 *
					 * ------------------------------------------------------- */
					$pgs_woo_api_options_data_url = pgs_woo_api_sample_data_url($sample_id, 'pgs_woo_api.json');
					$response = wp_remote_get( $pgs_woo_api_options_data_url, array( 'timeout' => 2000 ) );
					$response_body = json_decode( wp_remote_retrieve_body( $response ), true );
					if(isset($response_body['response']['show_notice'])){
						if(!empty($response_body['response']['show_notice'])){
							set_site_transient('pgs_woo_api_plugin_show_key_notice', true );
							update_option( 'pgs_woo_api_plugin_show_key_notice', true );
							set_site_transient('pgs_woo_api_plugin_show_key_notice_count', $response_body['response']['total_count'] );
							update_option( 'pgs_woo_api_plugin_show_key_notice_count', $response_body['response']['total_count'] );
						} else {
							update_option('pgs_woo_api_plugin_show_key_notice', false );
							delete_site_transient('pgs_woo_api_plugin_show_key_notice');
							delete_site_transient('pgs_woo_api_plugin_show_key_notice_count');
							delete_option( 'pgs_woo_api_plugin_show_key_notice_count' );
						}
						$message = esc_html__('Oops something went wrong.', 'pgs-woo-api');
                        $import_status_data = array(
							'success'     => false,
							//'message'     => $message. ' Error: ' . $pgs_woo_api_options_data_url->get_error_message() ."\r\n".$pgs_woo_api_options_data_url,
							'alert_msg'   =>  pgs_woo_api_ajax_admin_notice_render($message,'error')
						);
						wp_send_json($import_status_data);
    					die();
					}
					$pgs_woo_api_options_data = download_url($pgs_woo_api_options_data_url);

                    if ( !is_wp_error($pgs_woo_api_options_data) && file_exists($pgs_woo_api_options_data) ){
						$pgs_woo_api_options_json = $wp_filesystem->get_contents( $pgs_woo_api_options_data );
						$pgs_woo_api_options = json_decode( $pgs_woo_api_options_json, true );


                        $app_logo_light_id = pgs_woo_api_import_images_id($sample_id,'app_logo_light.png');
                        $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['app_logo_light']=$app_logo_light_id;

                        $app_logo = pgs_woo_api_import_images_id($sample_id,'app_logo.png');
                        $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['app_logo']=$app_logo;


                        //main_category
						$main_category = $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['main_category'];
                        if(isset($main_category) && !empty($main_category)){
				            foreach($main_category as $k => $v){
                                $main_cat_id = pgs_woo_api_insert_new_cat($v['main_cat_name']);
                                $product_app_cat_thumbnail_id = pgs_woo_api_import_images_id($sample_id,'icon-'.sanitize_title($v['main_cat_name']).'.png');
                                update_term_meta($main_cat_id, 'product_app_cat_thumbnail_id', $product_app_cat_thumbnail_id);
                                $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['main_category'][$k]['main_cat_id'] = $main_cat_id;
                                $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['main_category'][$k]['product_app_cat_thumbnail_id'] = $product_app_cat_thumbnail_id;
                            }
                        }


                        //main_slider
                        $main_slider = $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['main_slider'];
                        if(isset($main_slider) && !empty($main_slider)){
				            foreach($main_slider as $mk => $mv){
                                $slider_cat_id = pgs_woo_api_insert_new_cat($mv['main_cat_name']);
                                $upload_image_id = pgs_woo_api_import_images_id($sample_id,'slider-'.sanitize_title($mv['main_cat_name']).'.jpg');
                                $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['main_slider'][$mk]['upload_image_id'] = $upload_image_id;
                                $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['main_slider'][$mk]['slider_cat_id'] = $slider_cat_id;
                                $vsrc = wp_get_attachment_image_src($upload_image_id, 'large' );
                                if(!empty($vsrc)){
                                    $upload_image_url = esc_url($vsrc[0]);
                                } else {
                                    $upload_image_url = '';
                                }
                                $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['main_slider'][$mk]['upload_image_url'] = $upload_image_url;

                            }
                        }

                        //category_banners
                        $category_banners = $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['category_banners'];
                        if(isset($category_banners) && !empty($category_banners)){
				            foreach($category_banners as $ck => $cv){
				                $cat_banners_cat_id = pgs_woo_api_insert_new_cat($cv['main_cat_name']);
                                $cat_banners_image_id = pgs_woo_api_import_images_id($sample_id,'category-banners-'.sanitize_title($cv['main_cat_name']).'.jpg');
                                $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['category_banners'][$ck]['cat_banners_image_id'] = $cat_banners_image_id;
                                $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['category_banners'][$ck]['cat_banners_title'] = $cv['cat_banners_title'];
                                $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['category_banners'][$ck]['cat_banners_cat_id'] = $cat_banners_cat_id;
                                $vsrc = wp_get_attachment_image_src($cat_banners_image_id, 'thumbnail' );
                                if(!empty($vsrc)){
                                    $upload_image_url = esc_url($vsrc[0]);
                                } else {
                                    $upload_image_url = '';
                                }
                                $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['category_banners'][$mk]['cat_banners_image_url'] = $upload_image_url;

                            }
                        }

                        //banner_ad
                        $banner_ad = $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['banner_ad'];
                        if(isset($banner_ad) && !empty($banner_ad)){
				            foreach($banner_ad as $bk => $bv){
				                $banner_ad_cat_id = pgs_woo_api_insert_new_cat($bv['main_cat_name']);
                                $banner_ad_image_id = pgs_woo_api_import_images_id($sample_id,'banner-ads-'.sanitize_title($bv['main_cat_name']).'.jpg');
                                $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['banner_ad'][$bk]['banner_ad_image_id'] = $banner_ad_image_id;
                                $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['banner_ad'][$bk]['banner_ad_cat_id'] = $banner_ad_cat_id;

                                $vsrc = wp_get_attachment_image_src($banner_ad_image_id, 'thumbnail' );
                                if(!empty($vsrc)){
                                    $upload_image_url = esc_url($vsrc[0]);
                                } else {
                                    $upload_image_url = '';
                                }
                                $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['banner_ad'][$bk]['banner_ad_image_url'] = $upload_image_url;

                            }
                        }

                        //static_page
                        $pageid = pgs_woo_api_create_info_page_sample('About us');
                        $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['static_page']['about_us'] = $pageid;

                        $pageid = pgs_woo_api_create_info_page_sample('Terms of Use');
                        $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['static_page']['terms_of_use'] = $pageid;

                        $pageid = pgs_woo_api_create_info_page_sample('Privacy Policy');
                        $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['static_page']['privacy_policy'] = $pageid;



                        $feature_box = $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['feature_box'];
                        if(isset($feature_box) && !empty($feature_box)){
				            $feature_box_status = $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['feature_box_status'];
                            foreach($feature_box as $fk => $fv){
                                if($feature_box_status == 'enable'){
                                    $feature_image_id = pgs_woo_api_import_images_id($sample_id,'feature-box-'.$fk.'.png');
                                } else {
                                    $feature_image_id = '';
                                }
                                $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['feature_box'][$fk]['feature_image_id'] = $feature_image_id;
                                $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['feature_box'][$fk]['feature_title'] = $fv['feature_title'];
                                $pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']['feature_box'][$fk]['feature_content'] = $fv['feature_content'];
                            }
                        }

                        global $pgs_woo_api_array_replace_data;
						$pgs_woo_api_array_replace_data['old'] = $sample_data['demo_url'];
						$pgs_woo_api_array_replace_data['new'] = home_url( '/' );
						$pgs_woo_api_options = array_map("pgs_woo_api_replace_array", $pgs_woo_api_options);


                        update_option('pgs_woo_api_home_option',$pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_home_option']);
                        update_option( 'pgs_woo_api_app_assets_options',$pgs_woo_api_options['ciyashop_app_sample_data']['pgs_woo_api_app_assets_options'] );
                        do_action( 'pgs_woo_api_sample_data_import_plugin_options', $pgs_woo_api_options );

                        // save installed demo in DB
						$default_sample_data = array();
						$pgs_woo_api_sample_data_arr = get_option( 'pgs_woo_api_default_sample_data_arr' );
						if((isset($pgs_woo_api_sample_data_arr) && !empty($pgs_woo_api_sample_data_arr))){
							$default_pgs_woo_api_sample_data = json_decode($pgs_woo_api_sample_data_arr);
							if(!in_array($sample_id,$default_pgs_woo_api_sample_data)){
								$default_pgs_woo_api_sample_data[] = $sample_id;
							}
						} else {
							$default_pgs_woo_api_sample_data[] = $sample_id;
						}
						update_option( 'pgs_woo_api_default_sample_data_arr', json_encode($default_pgs_woo_api_sample_data ));
                        update_option( 'pgs_woo_api_default_sample_data_active', $sample_id);

                        $message = esc_html__('Sample data successfully imported.', 'pgs-woo-api');
    					$import_status_data = array(
    						'success'     => true,
    						'message'     => esc_html__('All done.','pgs-woo-api'),
    					    'alert_msg'  =>  pgs_woo_api_ajax_admin_notice_render($message,'success')
                        );

                    } else {
                        $message = esc_html__('Unable to get options file.', 'pgs-woo-api');
                        //if( defined('PGS_DEV_DEBUG') && PGS_DEV_DEBUG ){
							$import_status_data = array(
								'success'     => false,
								//'message'     => $message. ' Error: ' . $pgs_woo_api_options_data_url->get_error_message() ."\r\n".$pgs_woo_api_options_data_url,
                                'alert_msg'   =>  pgs_woo_api_ajax_admin_notice_render($message,'error')
                            );
						//}
					}

			}
		}
	}

	// Outout if import called from wizard.
	if( $action_source == 'wizard' ){
	}

	wp_send_json($import_status_data);
    die();
}

/**
 * Create new page for sample data return page id
 */
function pgs_woo_api_create_info_page_sample($title=''){

    $pageid = '';
    $info_page = get_page_by_title( 'App '.$title );
    if( isset($info_page) && !empty($info_page) ) {
        if(isset($info_page->ID)){
            $pageid = $info_page->ID;
        }
        return $pageid;
    } else {
        $dummy_content = "Lorem Ipsum is simply dummy text of the printing and typesetting industry. Lorem Ipsum has been the industry standard dummy text ever since the 1500s, when an unknown printer took a galley of type and scrambled it to make a type specimen book. It has survived not only five centuries, but also the leap into electronic typesetting, remaining essentially unchanged. It was popularised in the 1960s with the release of Letraset sheets containing Lorem Ipsum passages, and more recently with desktop publishing software like Aldus PageMaker including versions of Lorem Ipsum.";
        $pgs_page = array(
            'post_type' => 'page',
            'post_title'    => 'App '.$title,
            'post_content'  => $dummy_content,
            'post_status'   => 'publish',
        );
        // Insert the page into the database.
        $pageid = wp_insert_post( $pgs_page );
    }
    return $pageid;
}


/**
 * Create new category for sample data items
 */
 function pgs_woo_api_insert_new_cat($term_labal,$term_slug='') {
	$term = wp_insert_term( $term_labal, 'product_cat' );
    if ( is_wp_error( $term ) ) {
    	$term_id = (isset($term->error_data['term_exists']))?$term->error_data['term_exists']:null;
    } else {
    	$term_id = $term['term_id'];
    }
    return $term_id;
}

/**
 * inseted new images id for sample data
 */
function pgs_woo_api_import_images_id($sample_id,$img){

    $imge_url = PGS_API_URL.'inc/sample_data/'.$sample_id.'/'.$img;

    if(!empty($imge_url)){
			$upload_image_id = pgs_woo_api_up_and_get_upload_image($imge_url);
	}
    return (!empty($upload_image_id) ? $upload_image_id : "");
}


/**
 * Inset new images for sample data
 */
function pgs_woo_api_up_and_get_upload_image($image_url){
	$image = $image_use = str_replace("\"", "", $image_url);

	$extension = pathinfo( $image, PATHINFO_EXTENSION );
	$type = '';

	if ( $extension == "jpg" || $extension == "jpeg" ) {
		$type = "image/jpg";
	} elseif ( $extension == "png" ) {
		$type = "image/png";
	} elseif ( $extension == "gif" ) {
		$type = "image/gif";
	}

	if ( empty( $extension ) ) {
		$content_type = $type;
		if ( strstr( $content_type, "image/jpg" ) || strstr( $content_type, "image/jpeg" ) ) {
			$image_use = $image . ".jpg";
			$type      = "image/jpg";
		} elseif ( strstr( $content_type, "image/png" ) ) {
			$image_use = $image . ".png";
			$type      = "image/png";
		} elseif ( strstr( $content_type, "image/gif" ) ) {
			$image_use = $image . ".gif";
			$type      = "image/gif";
		}
	}

    $img_args = array(
		'timeout'   => 50,
		'redirection' => 0,
		'httpversion' => '1.1',
		'sslverify' => false,
		'stream' => false,
	);
	$get   = wp_remote_get( $image , $img_args );

	$mirror = wp_upload_bits( basename( $image_use ), '', wp_remote_retrieve_body( $get ) );
	$attachment = array(
		'post_title'     => basename( $image ),
		'post_mime_type' => $type
	);
	if ( isset( $mirror ) && ! empty( $mirror ) ) {
		$attach_id = wp_insert_attachment( $attachment, $mirror['file'] );
		require_once( ABSPATH . 'wp-admin/includes/image.php' );
		$attach_data = wp_generate_attachment_metadata( $attach_id, $mirror['file'] );
		wp_update_attachment_metadata( $attach_id, $attach_data );
	} else {
		$attach_id = "";
	}
	return $attach_id;

}



function pgs_woo_api_replace_array($n){
	global $pgs_woo_api_array_replace_data;

	if( is_array($n) ){
		return array_map("pgs_woo_api_replace_array", $n);
	}else{
		if( !empty($pgs_woo_api_array_replace_data) && is_array($pgs_woo_api_array_replace_data) && isset($pgs_woo_api_array_replace_data['old'])&& isset($pgs_woo_api_array_replace_data['new']) ){
			if (strpos($n, $pgs_woo_api_array_replace_data['old']) !== false) {
				return str_replace($pgs_woo_api_array_replace_data['old'],$pgs_woo_api_array_replace_data['new'],$n);
			}else{
				return $n;
			}
		}else{
			return $n;
		}
	}
    return $n;
}

function pgs_woo_api_sample_data_url( $sample_id = '', $resource = '' ){

    $pgs_token_android = get_option('pgs_woo_api_pgs_token_android');
    $item_key = '';
    if( $pgs_token_android && !empty($pgs_token_android)){
        $item_key = 'c7ec1dc95001d57cdedfe122569648dc';
	}
    $pgs_token_ios = get_option('pgs_woo_api_pgs_token_ios');

    if( $pgs_token_ios && !empty($pgs_token_ios)){
        $item_key = '7884626eb301b0f657bb23894fd2dbfe';
    }

    if( !empty($pgs_token_android) && !empty($pgs_token_ios)){
        $item_key = 'c7ec1dc95001d57cdedfe122569648dc';
    }

    // bail early if sample_id or resource not provided
	if( empty($sample_id) || empty($resource) ) return '';

	$purchase_token= pgs_woo_api_is_activated();

	return add_query_arg( array(
		'sample_id'     => $sample_id, // default
		'content'       => $resource,  // sample_data.xml
		'token'         => $purchase_token,
		'site_url'      => get_site_url(),
		'product_key'   => $item_key,
	), trailingslashit(PGS_ENVATO_API) . 'sample-data' );
}



function pgs_woo_api_sample_data_required_plugins_list(){
	global $pgs_woo_api_globals;


	$required_plugins_list = array();

	if( function_exists('pgs_woo_api_tgmpa_plugins_data') ){
		$plugins = pgs_woo_api_tgmpa_plugins_data();

		/*$pgs_woo_api_tgmpa_plugins_data_all = $pgs_woo_api_tgmpa_plugins_data['all'];
		foreach( $pgs_woo_api_tgmpa_plugins_data_all as $pgs_woo_api_tgmpa_plugins_data_k => $pgs_woo_api_tgmpa_plugins_data_v ){
			if( !$pgs_woo_api_tgmpa_plugins_data_v['required'] ){
				unset($pgs_woo_api_tgmpa_plugins_data_all[$pgs_woo_api_tgmpa_plugins_data_k]);
			}
		}

		if( !empty($pgs_woo_api_tgmpa_plugins_data_all) && is_array($pgs_woo_api_tgmpa_plugins_data_all) ){
			foreach( $pgs_woo_api_tgmpa_plugins_data_all as $pgs_woo_api_tgmpa_plugin ){
				$required_plugins_list[] = $pgs_woo_api_tgmpa_plugin['name'];
			}
		}*/

		$instance = call_user_func( array( get_class( $GLOBALS['pgs_api_tgmpa'] ), 'get_instance' ) );
        $plugins  = array(
			'all'      => array(), // Meaning: all plugins which still have open actions.
			'install'  => array(),
			'update'   => array(),
			'activate' => array(),
		);


        foreach ( $instance->plugins as $slug => $plugin ) {
            $is_plugin_check_active = pgs_is_plugin_check_active_for_sample_data($slug);
            if ( $is_plugin_check_active && false === $instance->does_plugin_have_update( $slug ) ) {
				// No need to display plugins if they are installed, up-to-date and active.
				continue;
			} else {

                $plugins['all'][ $slug ] = $plugin;
				if ( ! $instance->is_plugin_installed( $slug ) ) {
					$plugins['install'][ $slug ] = $plugin;
				} else {
					if ( false !== $instance->does_plugin_have_update( $slug ) ) {
						$plugins['update'][ $slug ] = $plugin;
					}

					if ( $instance->can_plugin_activate( $slug ) ) {
						$plugins['activate'][ $slug ] = $plugin;
					}
				}
			}
		}


        $pgs_woo_api_plugins = array();
        foreach($plugins['all'] as $key => $plugin){
		    if($plugin['slug'] == "rest-api"){
		        $pgs_woo_api_plugins[$key] = $plugins['all'][$key];
		    } elseif($plugin['slug'] == "rest-api-oauth1"){
		        $pgs_woo_api_plugins[$key] = $plugins['all'][$key];
		    } else {
		        unset($plugins['all'][$key]);
		    }
		}
        if(!empty($pgs_woo_api_plugins)){
            unset($plugins['all']);
            $plugins['all'] = $pgs_woo_api_plugins;
        }
	}

    foreach($plugins['all'] as $key => $plugin){
        $required_plugins_list[] = $plugin['name'];
    }

	return $required_plugins_list;
}

function pgs_is_plugin_check_active_for_sample_data( $slug ) {
	$instance = call_user_func( array( get_class( $GLOBALS['pgs_api_tgmpa'] ), 'get_instance' ) );
    return ( ( ! empty( $instance->plugins[ $slug ]['is_callable'] ) && is_callable( $instance->plugins[ $slug ]['is_callable'] ) ) || pgs_woo_api_widzard_check_plugin_active( $instance->plugins[ $slug ]['file_path'] ) );
}

function pgs_woo_api_sample_import_templates() {
	include_once trailingslashit(PGS_API_PATH) . "inc/sample_data/templates/pgs-woo-api-sample-import-alert.php";
}
add_action( "admin_footer", "pgs_woo_api_sample_import_templates" );
