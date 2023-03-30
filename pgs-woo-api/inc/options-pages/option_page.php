<?php
/**
 * Add Home Api
 */
add_action('admin_menu', 'pgs_woo_api_option_page_menu');
function pgs_woo_api_option_page_menu(){
	add_menu_page( esc_html('PGS Woo API','pgs-woo-api'), esc_html('App Settings','pgs-woo-api'),'manage_options','pgs-woo-api-settings','pgs_woo_api_callback');
}

function pgs_woo_api_callback(){

    require_once( PGS_API_PATH . 'inc/options-pages/option_functions.php' );
    $pgs_woo_api_home_option = array();
    $pgs_woo_api_home_option = get_option('pgs_woo_api_home_option');

    //get app color options
    $app_assets_options = get_option('pgs_woo_api_app_assets_options');

    $primary_color = (isset($app_assets_options['app_assets']['app_color']['primary_color']))?$app_assets_options['app_assets']['app_color']['primary_color']:'';
    $secondary_color = (isset($app_assets_options['app_assets']['app_color']['secondary_color']))?$app_assets_options['app_assets']['app_color']['secondary_color']:'';
    $header_color = (isset($app_assets_options['app_assets']['app_color']['header_color']))?$app_assets_options['app_assets']['app_color']['header_color']:'';

    $feature_product_status = "enable";
    $recent_product_status = "enable";
    $special_deal_product_status = "enable";


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
        global $sitepress;
        $language_negotiation_type = $sitepress->get_setting( 'language_negotiation_type' );
        if($language_negotiation_type != 1){
            $return = false;
            $message  = esc_html__( 'Current WPML Language URL format settings not compatible with PGS Woo API.', 'pgs-woo-api' );
            $message .= ' <a href="'.admin_url('admin.php?page=sitepress-multilingual-cms/menu/languages.php').'">';
            $message .= esc_html__( 'Click here for change settings.','pgs-woo-api');
            $message .= '</a>';
            echo pgs_woo_api_admin_notice_render( $message,'error' );
        }
    }
    $products_carousel = pgs_woo_api_get_products_carousel();
    ?>
    <div class="wrap pgs-woo-api-options-page">
		<h2></h2>		<?php echo do_action('pgs_woo_api_promo_notice'); ?>
        <div class="wrap-top gradient-bg">
            <h2 class="wp-heading-inline"><?php esc_html_e('App Settings','pgs-woo-api')?></h2>
            <div class="pgs-woo-api-right">
              <div class="publish-btn-box">
                  <span class="spinner"></span>
                  <button id="publish-btn" type="submit" name="submit-api" form="pgs-woo-api-form" class="pgs-woo-api-btn button button-primary Submit-btn" value="Submit"><?php esc_html_e('Save Changes','pgs-woo-api')?></button>
              </div>
              <div class="mobile-screen-view"></div>
            </div>
        </div>

        <form action="" method="post" name="pgs-woo-api-form" id="pgs-woo-api-form">
            <div id="pgs-woo-api-tabs">
                <ul>
                    <?php if ( $lang == '' ) { ?>
                        <li><a href="#pgs-woo-api-tabs-app-home-layout"><?php esc_html_e('Options','pgs-woo-api')?></a></li>
                    <?php } ?>
                    <li><a href="#pgs-woo-api-tabs-app-primary-logo"><?php esc_html_e('Primary Logo','pgs-woo-api')?></a></li>
                    <li><a href="#pgs-woo-api-tabs-app-secondary-logo"><?php esc_html_e('Secondary Logo','pgs-woo-api')?></a></li>
                    <?php if($lang == ''){?>
                        <li><a href="#pgs-woo-api-tabs-app-color"><?php esc_html_e('App Color','pgs-woo-api')?></a></li>
                        <li><a href="#pgs-woo-api-tabs-main-category-menu"><?php esc_html_e('Main Category Menu','pgs-woo-api')?></a></li>
                    <?php }?>
                    <li><a href="#pgs-woo-api-tabs-home-slider"><?php esc_html_e('Home Slider Banner','pgs-woo-api')?></a></li>
                    <li><a href="#pgs-woo-api-tabs-category-banners"><?php esc_html_e('Category Banners','pgs-woo-api')?></a></li>
                    <li><a href="#pgs-woo-api-tabs-products-carousel"><?php esc_html_e('Products View (Carousel)','pgs-woo-api')?></a></li>
                    <li><a href="#pgs-woo-api-tabs-banner-ads"><?php esc_html_e('Banner Ads','pgs-woo-api')?></a></li>
                    <li><a href="#pgs-woo-api-tabs-feature-box"><?php esc_html_e('Feature Box','pgs-woo-api')?></a></li>
                    <?php if($lang == ''){?>
                    <li><a href="#pgs-woo-api-tabs-app-pages"><?php esc_html_e('App Pages','pgs-woo-api')?></a></li>
                    <li><a href="#pgs-woo-api-tabs-checkout-page"><?php esc_html_e('Checkout Page','pgs-woo-api')?></a></li>
                    <li><a href="#pgs-woo-api-tabs-social-links"><?php esc_html_e('Social Links','pgs-woo-api')?></a></li>
                    <li><a href="#pgs-woo-api-tabs-contact-info"><?php esc_html_e('Contact Info','pgs-woo-api')?></a></li>
                    <li><a href="#pgs-woo-api-tabs-whatsapp"><?php esc_html_e('WhatsApp','pgs-woo-api')?></a></li>
                    <li><a href="#pgs-woo-api-tabs-sample-data"><?php esc_html_e('Sample Data','pgs-woo-api')?></a></li>
                    <li><a href="#pgs-woo-api-tabs-is-wpml"><?php esc_html_e('WPML','pgs-woo-api')?></a></li>
					<li><a href="#pgs-woo-api-custom-section-layout"><?php esc_html_e('Custom Section','pgs-woo-api')?></a></li>
					<?php /*?><li><a href="#pgs-woo-api-tabs-is-web-view"><?php esc_html_e('Web View','pgs-woo-api')?></a></li><?php */?>
                    <li><a href="#pgs-woo-api-tabs-filters"><?php esc_html_e('Filters','pgs-woo-api')?></a></li>
                    <li><a href="#pgs-woo-api-tabs-wallet"><?php esc_html_e( 'Wallet', 'pgs-woo-api' ); ?></a></li>
                    <?php }?>
                    <li><a href="#pgs-woo-api-delete-account"><?php esc_html_e( 'Delete account', 'pgs-woo-api' ); ?></a></li>
                </ul>

                <?php if ( $lang == '' ) { ?>
                    <div id="pgs-woo-api-tabs-app-home-layout">

                        <div class="pgs-woo-api-panel" id="pgs-woo-api-home_layout">
                            <div class="pgs-woo-api-panel-body">
                                <div class="pgs-woo-api-panel-heading"><?php esc_html_e('Configuration option','pgs-woo-api')?></div>
                                <p class="description"><?php esc_html_e('Select screen layout type.','pgs-woo-api')?></p>
                                <?php
                                $home_layout = (isset($pgs_woo_api_home_option['pgs_woo_api_home_layout']) && !empty($pgs_woo_api_home_option['pgs_woo_api_home_layout']))?$pgs_woo_api_home_option['pgs_woo_api_home_layout']:'default';

                                $scroll_is_login = (isset($pgs_woo_api_home_option['pgs_woo_api_scroll_is_login']) && !empty($pgs_woo_api_home_option['pgs_woo_api_scroll_is_login']))?$pgs_woo_api_home_option['pgs_woo_api_scroll_is_login']:'disable';

                                $scroll_is_slider = (isset($pgs_woo_api_home_option['pgs_woo_api_scroll_is_slider']) && !empty($pgs_woo_api_home_option['pgs_woo_api_scroll_is_slider']))?$pgs_woo_api_home_option['pgs_woo_api_scroll_is_slider']:'disable';

                                $add_to_cart = (isset($pgs_woo_api_home_option['pgs_woo_api_add_to_cart_option']) && !empty($pgs_woo_api_home_option['pgs_woo_api_add_to_cart_option']))?$pgs_woo_api_home_option['pgs_woo_api_add_to_cart_option']:'enable';

                                $catalog_mode = (isset($pgs_woo_api_home_option['pgs_woo_api_catalog_mode_option']) && !empty($pgs_woo_api_home_option['pgs_woo_api_catalog_mode_option']))?$pgs_woo_api_home_option['pgs_woo_api_catalog_mode_option']:'disable';

                                ?>
                                <div class="pgs-woo-api-form-groups radio-button-inline">
                                    <label><?php esc_html_e( 'Home screen layout type','pgs-woo-api' )?></label><br>
                                    <label><input type="radio" name="pgs[pgs_woo_api_home_layout]" class="pgs-woo-api-form-control home-layout-button-status" value="default" <?php echo ($home_layout == "default")?'checked=""':'';?>><?php esc_html_e( 'Default','pgs-woo-api' )?></label>
                                    <label><input type="radio" name="pgs[pgs_woo_api_home_layout]" class="pgs-woo-api-form-control home-layout-button-status" value="scroll" <?php echo ($home_layout == "scroll")?'checked=""':'';?>><?php esc_html_e( 'Infinite Product Scrolling','pgs-woo-api' )?></label>
                                    <p class="description"><?php esc_html_e('If you select the Default option, then Default "Home" page option will be visible in the Application. If you choose "Infinite Product Scrolling" then Instead home page, you will be able to see the Infinite scrolling with the random product.','pgs-woo-api')?></p></br>

                                    <div class="home-layout-scroll-options" >
                                        <label><?php esc_html_e( 'User Login','pgs-woo-api' )?></label><br>
                                        <label><input type="radio" name="pgs[pgs_woo_api_scroll_is_login]" class="pgs-woo-api-form-control" value="enable" <?php echo ($scroll_is_login == "enable")?'checked=""':'';?>><?php esc_html_e( 'Enable','pgs-woo-api' )?></label>
                                        <label><input type="radio" name="pgs[pgs_woo_api_scroll_is_login]" class="pgs-woo-api-form-control" value="disable" <?php echo ($scroll_is_login == "disable")?'checked=""':'';?>><?php esc_html_e( 'Disable','pgs-woo-api' )?></label>
                                        <p class="description"><?php esc_html_e('If you Enable this option then Whenever User opens the application, it will ask for Login/Register.','pgs-woo-api')?></p></br>

                                        <label><?php esc_html_e( 'Slider','pgs-woo-api' )?></label><br>
                                        <label><input type="radio" name="pgs[pgs_woo_api_scroll_is_slider]" class="pgs-woo-api-form-control" value="enable" <?php echo ($scroll_is_slider == "enable")?'checked=""':'';?>><?php esc_html_e( 'Enable','pgs-woo-api' )?></label>
                                        <label><input type="radio" name="pgs[pgs_woo_api_scroll_is_slider]" class="pgs-woo-api-form-control" value="disable" <?php echo ($scroll_is_slider == "disable")?'checked=""':'';?>><?php esc_html_e( 'Disable','pgs-woo-api' )?></label>
                                        <p class="description"><?php esc_html_e('If you Enable this option then Whenever User opens the application, it will show Introduction slider once.','pgs-woo-api')?></p></br>

                                        <label><?php esc_html_e( 'Add To Cart','pgs-woo-api' )?></label><br>
                                        <label><input type="radio" name="pgs[pgs_woo_api_add_to_cart_option]" class="pgs-woo-api-form-control" value="enable" <?php echo ($add_to_cart == "enable")?'checked=""':'';?>><?php esc_html_e( 'Enable','pgs-woo-api' )?></label>
                                        <label><input type="radio" name="pgs[pgs_woo_api_add_to_cart_option]" class="pgs-woo-api-form-control" value="disable" <?php echo ($add_to_cart == "disable")?'checked=""':'';?>><?php esc_html_e( 'Disable','pgs-woo-api' )?></label>
                                        <p class="description"><?php esc_html_e('If you Enable this option, then Application will show "Add to cart" button on the Product listing.','pgs-woo-api')?></p>
                                        </br>

                                        <label><?php esc_html_e( 'Catalog Mode','pgs-woo-api' )?></label><br>
                                        <label><input type="radio" name="pgs[pgs_woo_api_catalog_mode_option]" class="pgs-woo-api-form-control" value="enable" <?php echo ($catalog_mode == "enable")?'checked=""':'';?>><?php esc_html_e( 'Enable','pgs-woo-api' )?></label>
                                        <label><input type="radio" name="pgs[pgs_woo_api_catalog_mode_option]" class="pgs-woo-api-form-control" value="disable" <?php echo ($catalog_mode == "disable")?'checked=""':'';?>><?php esc_html_e( 'Disable','pgs-woo-api' )?></label>
                                        <p class="description">
                                        <?php printf(
                                            esc_html__( '%1$s %2$s', 'pgs-woo-api' ),
                                            esc_html__( 'Configuration option Video', 'pgs-woo-api' ),
                                            sprintf(
                                                '<a target="_blank" href="%s">%s</a>',
                                                esc_url( 'https://www.youtube.com/watch?v=u3114_QJs0c&list=PLplHaPmX0cKUsKcYd3oT1e9LhKdA7qHu2&index=36' ),
                                                esc_html__( 'here', 'pgs-woo-api' )
                                            )
                                        ); ?>
                                        </br>
                                        <?php printf(
                                            esc_html__( '%1$s %2$s', 'pgs-woo-api' ),
                                            esc_html__( 'Add to Cart Video', 'pgs-woo-api' ),
                                            sprintf(
                                                '<a target="_blank" href="%s">%s</a>',
                                                esc_url( 'https://www.youtube.com/watch?v=zARXC4ImAqA&list=PLplHaPmX0cKUsKcYd3oT1e9LhKdA7qHu2&index=38' ),
                                                esc_html__( 'here', 'pgs-woo-api' )
                                            )
                                        ); ?>
                                        </p>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div><!-- #pgs-woo-api-tabs-app-home-layout -->
                <?php } ?>


                <div id="pgs-woo-api-tabs-app-primary-logo">

                    <div class="pgs-woo-api-panel" id="pgs-woo-api-app_logo">
                        <div class="pgs-woo-api-panel-body">
                            <div class="pgs-woo-api-panel-heading"><?php esc_html_e('Primary Logo','pgs-woo-api')?></div>
							<p class="description"><?php esc_html_e('Logo display on the header of the app.','pgs-woo-api')?></p>
                            <?php $pgs_app_logo = $pgs_woo_api_home_option;?>
                            <p class="note-desc"><strong><?php esc_html_e('IMPORTANT','pgs-woo-api')?>:</strong><br />
								<span class="description"><?php esc_html_e('For best performance and image quality in the mobile application, strictly follow the below standards for logo images:','pgs-woo-api')?>
								</span><br />
							<?php esc_html_e('Dimension: ', 'pgs-woo-api')?> <strong><?php esc_html_e('164 x 51', 'pgs-woo-api')?></strong><br />
							<?php esc_html_e('File Type: ', 'pgs-woo-api')?> <strong><?php esc_html_e('PNG', 'pgs-woo-api')?></strong>
							</p>
                            <div class="pgs-woo-api-field-group">
                                <div class="pgs-woo-api-form-group">
                                    <?php
                                    if(!empty($lang)){
                                        $pgs_app_logo = get_option('pgs_woo_api_home_option_'.$lang);
                                    }
                                    if(isset($pgs_app_logo['app_logo_light']) && !empty($pgs_app_logo['app_logo_light']) ){
                                        $src = wp_get_attachment_image_src($pgs_app_logo['app_logo_light'], 'medium' );
                                        if(!empty($src)){
                                            ?>
                                            <div class="upload_image">
                                                <img src="<?php echo esc_url($src[0])?>" alt="No image" />
                                            </div>
                                            <input type="hidden" name="pgs[app_logo_light]" id="pgs[app_logo_light]" class="upload_image_id" value="<?php echo $pgs_app_logo['app_logo_light'];?>" />
                                            <a href="javascript:void(0);" class="upload-image-button button button-default" id="pgs-slider-app_logo_light"><?php esc_html_e( 'Edit','pgs-woo-api')?></a>
                                            <?php
                                        }
                                    } else {?>
                                        <div class="upload_image"></div>
                                        <input type="hidden" name="pgs[app_logo_light]" id="pgs[app_logo_light]" class="upload_image_id" value="" />
                                        <a href="javascript:void(0);" class="upload-image-button button button-primary" id="pgs-slider-app_logo_light"><?php esc_html_e( 'Add Image','pgs-woo-api')?></a>
                                        <?php
                                    }?>
                                    <a href="javascript:void(0);" class="remove-image-button button button-default" style="<?php echo ( ! @$pgs_app_logo['app_logo_light'] ? 'display:none;' : '' ); ?>"><?php esc_html_e( 'Remove','pgs-woo-api')?></a>
                                </div>
                            </div>
                        </div>
                        <div class="pgs-woo-api-panel-sidebar">
							<div class="device-select">
								<a href="javascript:void(0)" class="button button-primary android-device pgs-woo-device-img-display active" data-target="primary-logo-android-section">
									<img class="img-responsive" src="<?php echo PGS_API_URL.'img/android/android.png'?>" />
								</a>
								<a href="javascript:void(0)" class="button button-primary ios-device pgs-woo-device-img-display" data-target="primary-logo-ios-section">
									<img class="img-responsive" src="<?php echo PGS_API_URL.'img/ios/ios.png'?>" />
								</a>
							</div>
							<div id="primary-logo-android-section" class="device-display">
								<img src="<?php echo PGS_API_URL.'img/android/logo.png'?>" />
							</div>
							<div id="primary-logo-ios-section" class="device-display hidden">
								<img src="<?php echo PGS_API_URL.'img/ios/logo.png'?>" />
							</div>
                        </div>
                    </div>
                </div><!-- #pgs-woo-api-tabs-app-primary-logo -->
				<div id="pgs-woo-api-tabs-app-secondary-logo">

                    <div class="pgs-woo-api-panel" id="pgs-woo-api-app_logo">
                        <div class="pgs-woo-api-panel-body">
							<div class="pgs-woo-api-panel-heading">
								<?php esc_html_e('Secondary Logo','pgs-woo-api')?>
							</div>
							<p class="description"><?php esc_html_e('Logo display on other app pages like About Us and Contact Us.','pgs-woo-api')?></p>

                            <?php $pgs_app_logo = $pgs_woo_api_home_option;
                            if(!empty($lang)){
                                $pgs_app_logo = get_option('pgs_woo_api_home_option_'.$lang);
                            }
                            ?>
                            <p class="note-desc"><strong><?php esc_html_e('IMPORTANT','pgs-woo-api')?>:</strong><br />
								<span class="description"><?php esc_html_e('For best performance and image quality in the mobile application, strictly follow the below standards for logo images:','pgs-woo-api')?>
								</span><br />
							<?php esc_html_e('Dimension: ', 'pgs-woo-api')?> <strong><?php esc_html_e('164 x 51', 'pgs-woo-api')?></strong><br />
							<?php esc_html_e('File Type: ', 'pgs-woo-api')?> <strong><?php esc_html_e('PNG', 'pgs-woo-api')?></strong>
							</p>
                            <div class="pgs-woo-api-field-group">
                                <div class="pgs-woo-api-form-group">
                                    <?php
                                    if(isset($pgs_app_logo['app_logo']) && !empty($pgs_app_logo['app_logo']) ){
                                        $src = wp_get_attachment_image_src($pgs_app_logo['app_logo'], 'medium' );
                                        if(!empty($src)){
                                            ?>
                                            <div class="upload_image">
                                                <img src="<?php echo esc_url($src[0])?>" alt="No image" />
                                            </div>
                                            <input type="hidden" name="pgs[app_logo]" id="pgs[app_logo]" class="upload_image_id" value="<?php echo $pgs_app_logo['app_logo']; ?>" />
                                            <a href="javascript:void(0);" class="upload-image-button button button-default" id="pgs-slider-app_logo"><?php esc_html_e( 'Edit','pgs-woo-api')?></a>
                                            <?php
                                        }
                                    } else {?>
                                        <div class="upload_image"></div>
                                        <input type="hidden" name="pgs[app_logo]" id="pgs[app_logo]" class="upload_image_id" value="" />
                                        <a href="javascript:void(0);" class="upload-image-button button button-primary" id="pgs-slider-app_logo"><?php esc_html_e( 'Add Image','pgs-woo-api')?></a>
                                        <?php
                                    }?>
                                    <a href="javascript:void(0);" class="remove-image-button button button-default" style="<?php echo ( ! @$pgs_app_logo['app_logo'] ? 'display:none;' : '' ); ?>"><?php esc_html_e( 'Remove','pgs-woo-api')?></a>
                                </div>

                            </div>
                        </div>
                        <div class="pgs-woo-api-panel-sidebar">
							<div class="device-select">
								<a href="javascript:void(0)" class="button button-primary android-device pgs-woo-device-img-display active" data-target="secondary-logo-android-section">
									<img class="img-responsive" src="<?php echo PGS_API_URL.'img/android/android.png'?>" />
								</a>
								<a href="javascript:void(0)" class="button button-primary ios-device pgs-woo-device-img-display" data-target="secondary-logo-ios-section">
									<img class="img-responsive" src="<?php echo PGS_API_URL.'img/ios/ios.png'?>" />
								</a>
							</div>
							<div id="secondary-logo-android-section" class="device-display">
								<img src="<?php echo PGS_API_URL.'img/android/secondary-logo.png'?>" />
							</div>
							<div id="secondary-logo-ios-section" class="device-display hidden">
								<img src="<?php echo PGS_API_URL.'img/ios/secondary-logo.png'?>" />
							</div>
                        </div>
                    </div>
                </div><!-- #pgs-woo-api-tabs-app-logo -->

                <?php if($lang == ''){?>
                <div id="pgs-woo-api-tabs-app-color">
                    <div class="pgs-woo-api-panel">
                        <div class="pgs-woo-api-panel-body">
                        <div class="pgs-woo-api-panel-heading"><?php esc_html_e('App Color','pgs-woo-api')?></div><p class="description"><?php esc_html_e('You can change app default color and set as per your app design.','pgs-woo-api')?></p>

                            <!-- div class="pgs-woo-api-form-group">
                                <div class="form-field term-thumbnail-wrap radio-button-inline">
                                    <label><?php //esc_html_e('Header Color','pgs-woo-api')?></label>
                                    <div id="">
                                        <input type="text" id="header_color_code" class="cpa-color-picker" name="pgs_app_assets[app_color][header_color]" value="<?php echo esc_attr($header_color)?>"/>
                                    </div>
                                </div>
                            </div-->
                            <div class="pgs-woo-api-form-group">
                                <div class="form-field term-thumbnail-wrap radio-button-inline">
                                    <label><?php esc_html_e('Primary Color','pgs-woo-api')?></label>
                                    <div id="">
                                        <input type="text" id="primary_color_code" class="cpa-color-picker" name="pgs_app_assets[app_color][primary_color]" value="<?php echo esc_attr($primary_color)?>"/>
                                    </div>
									<p class="description"><?php esc_html_e('We recommend to select light color for batter look and feel.', 'pgs-woo-api' )?></p></br>
                                </div>
                            </div>
                            <div class="pgs-woo-api-form-group">
                                <div class="form-field term-thumbnail-wrap radio-button-inline">
                                    <label><?php esc_html_e('Secondary Color','pgs-woo-api')?></label>
                                    <div id="">
                                        <input type="text" id="secondary_color_code" class="cpa-color-picker" name="pgs_app_assets[app_color][secondary_color]" value="<?php echo esc_attr($secondary_color)?>"/>
                                    </div>
									<p class="description"><?php esc_html_e('We recommend to select dark color for batter look and feel.', 'pgs-woo-api' )?></p></br>
                                </div>
                            </div>

                        </div>
                        <div class="pgs-woo-api-panel-sidebar">
							<div class="device-select">
								<a href="javascript:void(0)" class="button button-primary android-device pgs-woo-device-img-display active" data-target="color-android-section">
									<img class="img-responsive" src="<?php echo PGS_API_URL.'img/android/android.png'?>" />
								</a>
								<a href="javascript:void(0)" class="button button-primary ios-device pgs-woo-device-img-display" data-target="color-ios-section">
									<img class="img-responsive" src="<?php echo PGS_API_URL.'img/ios/ios.png'?>" />
								</a>
							</div>
							<div id="color-android-section" class="device-display">
								<img src="<?php echo PGS_API_URL.'img/android/color.png'?>" />
							</div>
							<div id="color-ios-section" class="device-display hidden">
								<img src="<?php echo PGS_API_URL.'img/ios/color.png'?>" />
							</div>
                        </div>
                    </div>
                </div><!-- #pgs-woo-api-tabs-app-color -->

                <div id="pgs-woo-api-tabs-main-category-menu">

                    <div class="pgs-woo-api-panel" id="pgs-woo-api-main-category" data-limit="6">
                        <div class="pgs-woo-api-panel-body">
                            <div class="pgs-woo-api-panel-heading"><?php esc_html_e('Main Category Menu','pgs-woo-api')?></div>
                            <div class="pgs-woo-api-repeater-field-group" data-repeater-list="pgs[main_category]">
                                 <p class="description"><?php esc_html_e('Categories menu items display on the home screen.','pgs-woo-api')?></p>
                                <?php
                                $default_array['main_category'] = array(
                                    array(
                                        'main_cat_id' => '',
                                    )
                                );
                                $main_category = array();
                                $main_category = $pgs_woo_api_home_option;
                                if(!isset($main_category['main_category']) || empty($main_category['main_category'])){
                                    $main_category = $default_array;
                                }

                                if(isset($main_category['main_category']) && !empty($main_category['main_category'])){
                                    $i = 0;
                                    foreach($main_category['main_category'] as $key => $val){
                                        ?>
                                        <div class="pgs-woo-api-field-group" data-repeater-item>
                                            <div class="pgs-woo-api-form-group main-category">
                                                <label><?php esc_html_e('Select category menu item','pgs-woo-api')?></label>
                                                <select class="pgs-woo-api-form-control pgs-woo-api_main_category" name="pgs[main_category][<?php echo $i?>][main_cat_id]">
                                                    <option value=""><?php esc_html_e('Select','pgs-woo-api')?></option>
                                                    <?php
                                                    $img = '';
                                                    $select = pgs_woo_api_get_all_woo_cat();
                                                    if(isset($select) && !empty($select)){
                                                        $selected = '';
                                                        foreach($select as $sekey => $seval){
                                                            $sel = '';
                                                            if( defined( 'ICL_LANGUAGE_CODE' ) && function_exists( 'icl_object_id' ) && ($lang != '') ) {
                                                                $original_id = icl_object_id( $val['main_cat_id'], 'product_cat', false, $lang );
                                                                $val['main_cat_id'] = $original_id;
                                                                //$original_ob = get_term( $original_id, 'product_cat' );
                                                            }
                                                            if($val['main_cat_id'] == $sekey){
                                                                $sel = 'selected="selected"';
                                                            }
                                                            $caticon_url = 'data-caticon="'.pgs_woo_api_get_app_cat_icon_url($sekey,false).'"';
                                                            $caticon_id  = ' data-caticonid="'.pgs_woo_api_get_app_cat_icon_id($sekey,false).'"';
                                                            echo '<option value="'.$sekey.'" '.$sel.' '.$caticon_url.$caticon_id.'>'.$seval.'</option>';
                                                        }
                                                    }?>
                                                </select>
                                            </div>
                                            <div class="pgs-woo-api-form-group pgs-woo-api_main_category_icon" style="<?php echo ( ! @$val['main_cat_id']  ? 'display:none;' : '' ); ?>">
                                                <label><?php esc_html_e('Category Icon Image','pgs-woo-api')?></label>
                                                <?php $product_app_cat_thumbnail_id = 0;
                                                if(isset($val['main_cat_id']) && !empty($val['main_cat_id']) ){
                                                    $vsrc = array();$app_cat='';
                                                	$product_app_cat_thumbnail_id = get_term_meta($val['main_cat_id'], 'product_app_cat_thumbnail_id', true);
                                                    $vsrc = wp_get_attachment_image_src($product_app_cat_thumbnail_id, 'thumbnail' );
                                                    if(!empty($vsrc)){
                                                        ?>
                                                        <div class="upload_image">
                                                            <img src="<?php echo esc_url($vsrc[0])?>" alt="No image" width="70px" height="70px" />
                                                        </div>
                                                        <input type="hidden" name="pgs[main_category][<?php echo $i?>][product_app_cat_thumbnail_id]" id="pgs[product_app_cat_thumbnail_id][<?php echo $i?>]" class="upload_image_id" value="<?php echo $product_app_cat_thumbnail_id?>" />
                                                        <a href="javascript:void(0);" class="upload-image-button button button-default" id="product-app-cat-thumbnail-id-<?php echo $i?>" data-lbl="<?php esc_html_e( 'Edit','pgs-woo-api')?>"><?php esc_html_e( 'Edit','pgs-woo-api')?></a>
                                                        <?php
                                                    } else {?>
                                                        <div class="upload_image"></div>
                                                        <input type="hidden" name="pgs[main_category][<?php echo $i?>][product_app_cat_thumbnail_id]" id="pgs[product_app_cat_thumbnail_id][<?php echo $i?>]" class="upload_image_id" value="" />
                                                        <a href="javascript:void(0);" class="upload-image-button button button-primary" id="product-app-cat-thumbnail-id-<?php echo $i?>" data-lbl="<?php esc_html_e( 'Add Image','pgs-woo-api')?>"><?php esc_html_e( 'Add Image','pgs-woo-api')?></a>
                                                        <?php
                                                    }
                                                } else {?>
                                                    <div class="upload_image"></div>
                                                    <input type="hidden" name="pgs[main_category][<?php echo $i?>][product_app_cat_thumbnail_id]" id="pgs[product_app_cat_thumbnail_id][<?php echo $i?>]" class="upload_image_id" value="" />
                                                    <a href="javascript:void(0);" class="upload-image-button button button-primary" id="product-app-cat-thumbnail-id-<?php echo $i?>" <?php esc_html_e( 'Add Image','pgs-woo-api')?>><?php esc_html_e( 'Add Image','pgs-woo-api')?></a>
                                                    <?php
                                                }?>
                                                <a href="javascript:void(0);" class="remove-image-button button button-default" style="<?php echo ( $product_app_cat_thumbnail_id == 0 ? 'display:none;' : '' ); ?>"><?php esc_html_e( 'Remove','pgs-woo-api')?></a>
                                            </div>
                                            <span class="removeBanner dashicons dashicons-dismiss" data-repeater-delete><!-- Remove --></span>
                                        </div>
                                        <?php
                                        $i++;
                                    }
                                } ?>
                            </div>
                            <button type="button" class="pgs-woo-api-btn button button-primary" data-repeater-create><?php esc_html_e('Add','pgs-woo-api')?></button>
                        </div>

                        <div class="pgs-woo-api-panel-sidebar">
    						<div class="device-select">
    							<a href="javascript:void(0)" class="button button-primary android-device pgs-woo-device-img-display active" data-target="categories-android-section">
    								<img class="img-responsive" src="<?php echo PGS_API_URL.'img/android/android.png'?>" />
    							</a>
    							<a href="javascript:void(0)" class="button button-primary ios-device pgs-woo-device-img-display" data-target="categories-ios-section">
    								<img class="img-responsive" src="<?php echo PGS_API_URL.'img/ios/ios.png'?>" />
    							</a>
    						</div>
    						<div id="categories-android-section" class="device-display">
    							<img src="<?php echo PGS_API_URL.'img/android/categories.png'?>" />
    						</div>
    						<div id="categories-ios-section" class="device-display hidden">
    							<img src="<?php echo PGS_API_URL.'img/ios/categories.png'?>" />
    						</div>
    					</div>
                    </div>

                </div><!-- #pgs-woo-api-tabs-main-category-menu -->
                <?php }?>
                <div id="pgs-woo-api-tabs-home-slider">

                    <div class="pgs-woo-api-panel" id="pgs-woo-api-main_slider">
                        <div class="pgs-woo-api-panel-body">
                        <div class="pgs-woo-api-panel-heading"><?php esc_html_e('Home Slider Banner','pgs-woo-api')?></div>
                        <p class="description"><?php esc_html_e('These banners display as a slider on the home screen.','pgs-woo-api')?></p>
                        <p class="note-desc"><strong><?php esc_html_e('IMPORTANT','pgs-woo-api')?>:</strong><br />
							<span class="description">
							<?php
							echo wp_kses(
								__('For best performance and image quality of slider banners in the mobile application, strictly use <strong>1125 x 633</strong> (WidthxHeight in px) image for slider banners.','pgs-woo-api'),
								array(
									'strong' => array()
								)
							);
							?>
							</span>
						</p>
						<div class="pgs-woo-api-repeater-field-group" data-repeater-list="pgs[main_slider]">
                                <?php
                                $default_array['main_slider'] = array(
                                    array(
                                            'upload_image_id' => '',
                                            'slider_cat_id' => ''
                                    )
                                );
                                $pgs_main_slider = array();
                                $pgs_main_slider = $pgs_woo_api_home_option;
                                if(!empty($lang)){
                                    $pgs_main_slider = get_option('pgs_woo_api_home_option_'.$lang);
                                }

                                if(empty($pgs_main_slider['main_slider'])){
                                    $pgs_main_slider = $default_array;
                                }
                                if(isset($pgs_main_slider['main_slider']) && !empty($pgs_main_slider['main_slider'])){
                                    $i = 0;
                                    foreach($pgs_main_slider['main_slider'] as $key => $val){
                                        ?>
                                        <div  class="pgs-woo-api-field-group" id="main-slider-row-<?php echo $i?>" data-repeater-item>
                                            <div class="pgs-woo-api-form-group">
                                                <label><?php esc_html_e( 'Slider Image', 'pgs-woo-api' )?></label>
                                                <?php
                                                if(isset($val['upload_image_id']) && !empty($val['upload_image_id']) ){
                                                    $src = wp_get_attachment_image_src($val['upload_image_id'], 'medium' );
                                                    if(!empty($src)){
                                                        ?>
                                                        <div class="upload_image">
                                                            <img src="<?php echo esc_url($src[0])?>" alt="No image" />
                                                        </div>
                                                        <input type="hidden" name="pgs[main_slider][<?php echo $i?>][upload_image_id]" id="pgs[main_slider][<?php echo $i?>]" class="upload_image_id" value="<?php echo $val['upload_image_id']; ?>" />
                                                        <a href="javascript:void(0);" class="upload-image-button button button-default" id="pgs-slider-<?php echo $i?>"><?php esc_html_e( 'Edit','pgs-woo-api')?></a>
                                                        <?php
                                                    }
                                                } else {?>
                                                    <div class="upload_image"></div>
                                                    <input type="hidden" name="pgs[main_slider][<?php echo $i?>][upload_image_id]" id="pgs[main_slider][<?php echo $i?>]" class="upload_image_id" value="" />
                                                    <a href="javascript:void(0);" class="upload-image-button button button-primary" id="pgs-slider-<?php echo $i?>"><?php esc_html_e( 'Add Image','pgs-woo-api')?></a>
                                                    <?php
                                                }?>
                                                <a href="javascript:void(0);" class="remove-image-button button button-default" style="<?php echo ( ! $val['upload_image_id'] ? 'display:none;' : '' ); ?>"><?php esc_html_e( 'Remove','pgs-woo-api')?></a>
                                            </div>

                                            <div class="pgs-woo-api-form-group">
                                                <label><?php esc_html_e('Select Product Category','pgs-woo-api')?></label>
                                                <select class="pgs-woo-api-form-control pgs-woo-api_pages" name="pgs[main_slider][<?php echo $i?>][slider_cat_id]">
                                                    <option value=""><?php esc_html_e('Select','pgs-woo-api')?></option>
                                                    <?php
                                                    $select = pgs_woo_api_get_all_woo_cat();
                                                    if(isset($select) && !empty($select)){
                                                        $selected = '';
                                                        foreach($select as $sekey => $seval){
                                                            $sel = '';
                                                            if( defined( 'ICL_LANGUAGE_CODE' ) && function_exists( 'icl_object_id' ) && ($lang != '') ) {
                                                                $original_id = icl_object_id( $val['slider_cat_id'], 'product_cat', true, $lang );
                                                                $val['slider_cat_id'] = $original_id;
                                                                //$original_ob = get_term( $original_id, 'product_cat' );
                                                            }

                                                            if($val['slider_cat_id'] == $sekey){
                                                                $sel = 'selected="selected"';
                                                            }
                                                            echo '<option value="'.$sekey.'" '.$sel.'>'.$seval.'</option>';
                                                        }
                                                    }?>
                                                </select>
                                            </div>
                                            <span class="remove dashicons dashicons-dismiss" data-repeater-delete><!-- Remove Option --></span>
                                        </div>
                                        <?php
                                        $i++;
                                    }
                                } ?>

                            </div>
                            <button type="button" class="pgs-woo-api-btn button button-primary" data-id="<?php echo $i?>" data-repeater-create><?php esc_html_e('Add','pgs-woo-api')?></button>
                        </div>

                        <div class="pgs-woo-api-panel-sidebar">
							<div class="device-select">
								<a href="javascript:void(0)" class="button button-primary android-device pgs-woo-device-img-display active" data-target="main-banner-android-section">
									<img class="img-responsive" src="<?php echo PGS_API_URL.'img/android/android.png'?>" />
								</a>
								<a href="javascript:void(0)" class="button button-primary ios-device pgs-woo-device-img-display" data-target="main-banner-ios-section">
									<img class="img-responsive" src="<?php echo PGS_API_URL.'img/ios/ios.png'?>" />
								</a>
							</div>
							<div id="main-banner-android-section" class="device-display">
								<img src="<?php echo PGS_API_URL.'img/android/main-banner.png'?>" />
							</div>
							<div id="main-banner-ios-section" class="device-display hidden">
								<img src="<?php echo PGS_API_URL.'img/ios/main-banner.png'?>" />
							</div>
						</div>
                    </div>

              </div><!-- #pgs-woo-api-tabs-home-slider -->
              <div id="pgs-woo-api-tabs-category-banners">

                 <div class="pgs-woo-api-panel" id="pgs-woo-api-category-banners">
                    <div class="pgs-woo-api-panel-body">
                    <div class="pgs-woo-api-panel-heading"><?php esc_html_e('Category Banners','pgs-woo-api')?></div>
                    <p class="description"><?php esc_html_e( 'Banners displayed on the home screen.','pgs-woo-api' )?></p>
					<p class="note-desc"><strong><?php esc_html_e('IMPORTANT','pgs-woo-api')?>:</strong><br />
						<span class="description">
						<?php
						echo wp_kses(
							__('For best performance and image quality of category banners in the mobile application, strictly use <strong>420 x 305</strong> (WidthxHeight in px) image for category banners.','pgs-woo-api'),
							array(
								'strong' => array()
							)
						);
						?>
						</span>
					</p>
                   <div class="pgs-woo-api-repeater-field-group" data-repeater-list="pgs[category_banners]">
                        <?php
                        $default_array['category_banners'] = array(
                            array(
                                'cat_banners_image_id' => '',
                                'cat_banners_title' => '',
                                'cat_banners_cat_id' => ''
                            )
                        );
                        $pgs_category_banners = array();
                        $pgs_category_banners = $pgs_woo_api_home_option;

                        if(!empty($lang)){
                            $pgs_category_banners = get_option('pgs_woo_api_home_option_'.$lang);
                        }

                        if(empty($pgs_category_banners['category_banners'])){
                            $pgs_category_banners = $default_array;
                        }
                        if(isset($pgs_category_banners['category_banners']) && !empty($pgs_category_banners['category_banners'])){
                            $i = 0;
                            foreach($pgs_category_banners['category_banners'] as $key => $val){
                                ?>
                                <div class="pgs-woo-api-field-group" data-repeater-item>
                                    <div class="pgs-woo-api-form-group">
                                        <label><?php esc_html_e( 'Banner Image','pgs-woo-api' )?></label>
                                        <?php
                                        if(isset($val['cat_banners_image_id']) && !empty($val['cat_banners_image_id'])){
                                            $src = wp_get_attachment_image_src($val['cat_banners_image_id'], 'medium' );
                                            if(!empty($src)){
                                                ?>
                                                <div class="upload_image">
                                                    <img src="<?php echo esc_url($src[0])?>" alt="No image" />
                                                </div>
                                                <input type="hidden" name="pgs[category_banners][<?php echo $i?>][cat_banners_image_id]" class="upload_image_id" value="<?php echo $val['cat_banners_image_id']; ?>" />
                                                <a href="javascript:void(0);" class="upload-image-button button button-default" id="pgs-banner-upload-image-<?php echo $i?>"><?php esc_html_e( 'Edit','pgs-woo-api')?></a>
                                                <?php
                                            }
                                        } else {?>
                                            <div class="upload_image"></div>
                                            <input type="hidden" name="pgs[category_banners][<?php echo $i?>][cat_banners_image_id]" class="upload_image_id" value="" />
                                            <a href="javascript:void(0);" class="upload-image-button button button-primary" id="pgs-banner-upload-image-<?php echo $i?>"><?php esc_html_e( 'Add Image','pgs-woo-api')?></a>
                                            <?php
                                        }?>
                                        <a href="javascript:void(0);" class="remove-image-button button button-default" style="<?php echo ( !$val['cat_banners_image_id'] ? 'display:none;' : '' ); ?>"><?php esc_html_e( 'Remove','pgs-woo-api')?></a>
                                    </div>


                                    <div class="pgs-woo-api-form-group">
                                        <label><?php esc_html_e('Banner Title','pgs-woo-api')?></label>
                                        <input type="text" name="pgs[category_banners][<?php echo $i?>][cat_banners_title]" class="pgs-woo-api-form-control" value="<?php echo $val['cat_banners_title']; ?>"  />
                                    </div>


                                    <div class="pgs-woo-api-form-group">
                                        <label><?php esc_html_e('Select Product Category','pgs-woo-api')?></label>
                                        <select class="pgs-woo-api-form-control pgs-woo-api_pages" name="pgs[category_banners][<?php echo $i?>][cat_banners_cat_id]">
                                            <option value=""><?php esc_html_e('Select','pgs-woo-api')?></option>
                                            <?php
                                            $select = pgs_woo_api_get_all_woo_cat();
                                            if(isset($select) && !empty($select)){
                                                $selected = '';
                                                foreach($select as $sekey => $seval){
                                                    $sel = '';
                                                    if($val['cat_banners_cat_id'] == $sekey){
                                                        $sel = 'selected="selected"';
                                                    }
                                                    echo '<option value="'.$sekey.'" '.$sel.'>'.$seval.'</option>';
                                                }
                                            }?>
                                        </select>
                                    </div>
                                    <span class="removeBanner dashicons dashicons-dismiss" data-repeater-delete><!-- Remove --></span>
                                </div>
                                <?php
                                $i++;
                            }
                        } ?>

                        </div>
                        <button type="button" class="pgs-woo-api-btn button button-primary" data-repeater-create><?php esc_html_e('Add','pgs-woo-api')?></button>
                    </div>
                    <div class="pgs-woo-api-panel-sidebar">
						<div class="device-select">
							<a href="javascript:void(0)" class="button button-primary android-device pgs-woo-device-img-display active" data-target="category-banners-android-section">
								<img class="img-responsive" src="<?php echo PGS_API_URL.'img/android/android.png'?>" />
							</a>
							<a href="javascript:void(0)" class="button button-primary ios-device pgs-woo-device-img-display" data-target="category-banners-ios-section">
								<img class="img-responsive" src="<?php echo PGS_API_URL.'img/ios/ios.png'?>" />
							</a>
						</div>
						<div id="category-banners-android-section" class="device-display">
							<img src="<?php echo PGS_API_URL.'img/android/category-banner.png'?>" />
						</div>
						<div id="category-banners-ios-section" class="device-display hidden">
							<img src="<?php echo PGS_API_URL.'img/ios/category-banners.png'?>" />
						</div>
					</div>
                </div>
              </div><!-- #pgs-woo-api-tabs-category-banners -->


            <div id="pgs-woo-api-tabs-products-carousel">

                <div class="pgs-woo-api-panel" id="pgs-woo-api-products-carousel">
                    <div class="pgs-woo-api-panel-body">
                        <div class="pgs-woo-api-panel-heading"><?php esc_html_e('Products View','pgs-woo-api')?></div>
                        <div class="pgs-woo-api-repeater-field-group">
                            <p class="description"><?php esc_html_e('Products View display on the home screen. we can also drag the position up and down','pgs-woo-api')?></p>
                            <div id="pgs-expand-div-options-products-carousel" class="pgs-woo-api-sort-products-carousel">
                                <?php
                                foreach($products_carousel as $key => $carousel){
                                    ?>
                                    <div class="postbox">
                                        <div id="pwa-<?php echo $key?>" class="pgs-expand-div-btn">
                                            <button type="button" class="handlediv">
                                                <i class="dashicons dashicons-arrow-down"></i>
                                            </button>
                                            <h2 class="hndle ui-sortable-handle"><span><?php echo esc_html($carousel['label'])?></span><span class="is-disable-pwa-<?php echo $key?>"><?php echo ($carousel['status'] == "disable")?'<img src="'.esc_url(PGS_API_URL.'img/disabled01.png').'" alt="disable"/>':'';?></span></h2>
                                        </div>
                                        <div class="inside pgs-expand-div-content pgs-woo-api-hide">
                                        	<div class="main">
                                                <div class="pgs-woo-api-field-groups">
                                                    <div class="pgs-woo-api-form-groups radio-button-inline">
                                                        <label><?php echo esc_html($carousel['label'])?></label>
                                                        <p class="description"><?php echo esc_html($carousel['description'])?></p>
                                                        <?php echo (isset($carousel['doc_description']))?$carousel['doc_description']:'';?>
                                                    </div>
                                                    <div class="pgs-woo-api-form-groups radio-button-inline">
                                                        <label class="disable-data-url" data-url="<?php echo esc_url(PGS_API_URL.'img/disabled01.png') ?>"><?php esc_html_e("Status",'pgs-woo-api')?></label>
                                                        <label><input type="radio" name="pgs[products_carousel][<?php echo $key?>][status]" class="pgs-woo-api-form-control carousel-box-status" data-id="pwa-<?php echo $key?>" value="enable" <?php echo ($carousel['status'] == "enable")?'checked=""':'';?> /><?php esc_html_e( 'Enable','pgs-woo-api')?></label>
                                                        <label><input type="radio" name="pgs[products_carousel][<?php echo $key?>][status]" class="pgs-woo-api-form-control carousel-box-status" data-id="pwa-<?php echo $key?>" value="disable" <?php echo ($carousel['status'] == "disable")?'checked=""':'';?> /><?php esc_html_e( 'Disable','pgs-woo-api')?></label>
                                                    </div>
                                                    <div class="pgs-woo-api-form-groups">
                                                        <label><?php esc_html_e( 'Title','pgs-woo-api' );?></label>
                                                        <input type="text" name="pgs[products_carousel][<?php echo $key?>][title]" class="pgs-woo-api-form-control" value="<?php echo esc_html($carousel['title'])?>" />
                                                    </div>
                                                </div>
                                            </div>
                                    	</div>
                                    </div>
                                <?php  } ?>
                            </div>
                        </div>
                    </div>
                    <div class="pgs-woo-api-panel-sidebar">
						<div class="device-select">
							<a href="javascript:void(0)" class="button button-primary android-device pgs-woo-device-img-display active" data-target="products-carousel-android-section">
								<img class="img-responsive" src="<?php echo PGS_API_URL.'img/android/android.png'?>" />
							</a>
							<a href="javascript:void(0)" class="button button-primary ios-device pgs-woo-device-img-display" data-target="products-carousel-ios-section">
								<img class="img-responsive" src="<?php echo PGS_API_URL.'img/ios/ios.png'?>" />
							</a>
						</div>
						<div id="products-carousel-android-section" class="device-display">
							<img src="<?php echo PGS_API_URL.'img/android/products-carousel.png'?>" />
						</div>
						<div id="products-carousel-ios-section" class="device-display hidden">
							<img src="<?php echo PGS_API_URL.'img/ios/products-carousel.png'?>" />
						</div>
                    </div>
                </div>
            </div><!-- #pgs-woo-api-tabs-products-carousel -->


            <div id="pgs-woo-api-tabs-banner-ads">

                <div class="pgs-woo-api-panel" id="pgs-woo-api-banner-ad">
                    <div class="pgs-woo-api-panel-body">
                    <div class="pgs-woo-api-panel-heading"><?php esc_html_e('Banner Ads','pgs-woo-api')?></div>
                    <p class="description"><?php esc_html_e( 'Used for advertisement or special offers, displayed on Home screen.','pgs-woo-api')?></p>
					<p class="note-desc"><strong><?php esc_html_e('IMPORTANT','pgs-woo-api')?>:</strong><br />
						<span class="description">
						<?php
						echo wp_kses(
							__('For best performance and image quality of Ad banners in the mobile application, strictly use <strong>1080 x 353</strong> (WidthxHeight in px) image for banners.','pgs-woo-api'),
							array(
								'strong' => array()
							)
						);
						?>
						</span>
					</p>

                        <div class="pgs-woo-api-repeater-field-group" data-repeater-list="pgs[banner_ad]">
                        <?php
                        $default_array['banner_ad'] = array(
                            array(
                                'banner_ad_cat_id' => '',
                                'banner_ad_image_id' => ''
                            )
                        );
                        $banner_ad = array();
                        $banner_ad = $pgs_woo_api_home_option;

                        if(!empty($lang)){
                            $banner_ad = get_option('pgs_woo_api_home_option_'.$lang);
                        }

                        if(!isset($banner_ad['banner_ad']) || empty($banner_ad['banner_ad'])){
                            $banner_ad = $default_array;
                        }

                        if(isset($banner_ad['banner_ad']) && !empty($banner_ad['banner_ad'])){
                            $i = 0;
                            foreach($banner_ad['banner_ad'] as $key => $val){

                                ?>
                                <div class="pgs-woo-api-field-group" data-repeater-item>
                                    <div class="pgs-woo-api-form-group">
                                        <label><?php esc_html_e( 'Banner Image','pgs-woo-api')?></label>
                                        <?php
                                        if(isset($val['banner_ad_image_id']) && !empty($val['banner_ad_image_id']) ){
                                            $src = wp_get_attachment_image_src($val['banner_ad_image_id'], 'medium' );
                                            if(!empty($src)){
                                                ?>
                                                <div class="upload_image">
                                                    <img src="<?php echo esc_url($src[0])?>" alt="No image" />
                                                </div>
                                                <input type="hidden" name="pgs[banner_ad][<?php echo $i?>][banner_ad_image_id]" id="pgs[banner_ad][<?php echo $i?>]" class="upload_image_id" value="<?php echo $val['banner_ad_image_id']; ?>" />
                                                <a href="javascript:void(0);" class="upload-image-button button button-default" id="pgs-slider-<?php echo $i?>"><?php esc_html_e( 'Edit','pgs-woo-api')?></a>
                                                <?php
                                            }
                                        } else {?>
                                            <div class="upload_image"></div>
                                            <input type="hidden" name="pgs[banner_ad][<?php echo $i?>][banner_ad_image_id]" id="pgs[banner_ad][<?php echo $i?>]" class="upload_image_id" value="" />
                                            <a href="javascript:void(0);" class="upload-image-button button button-primary" id="pgs-slider-<?php echo $i?>"><?php esc_html_e( 'Add Image','pgs-woo-api')?></a>
                                            <?php
                                        }?>
                                        <a href="javascript:void(0);" class="remove-image-button button button-default" style="<?php echo ( ! $val['banner_ad_image_id'] ? 'display:none;' : '' ); ?>"><?php esc_html_e( 'Remove','pgs-woo-api')?></a>
                                    </div>

                                    <div class="pgs-woo-api-form-group">
                                        <label><?php esc_html_e('Category for Banner Ad','pgs-woo-api')?></label>
                                        <select class="pgs-woo-api-form-control pgs-woo-api_pages" name="pgs[banner_ad][<?php echo $i?>][banner_ad_cat_id]">
                                            <option value=""><?php esc_html_e('Select','pgs-woo-api')?></option>
                                            <?php
                                            $img = '';
                                            $select = pgs_woo_api_get_all_woo_cat();
                                            if(isset($select) && !empty($select)){
                                                foreach($select as $sekey => $seval){
                                                    $sel = '';
                                                    if($val['banner_ad_cat_id'] == $sekey){
                                                        $sel = 'selected="selected"';
                                                    }
                                                    echo '<option value="'.$sekey.'" '.$sel.'>'.$seval.'</option>';
                                                }
                                            }?>
                                        </select>
                                    </div>
                                    <span class="removeBanner dashicons dashicons-dismiss" data-repeater-delete><!-- Remove --></span>
                                </div>
                                <?php
                                $i++;
                            }
                        } ?>

                        </div>
                        <button type="button" class="pgs-woo-api-btn button button-primary" data-repeater-create><?php esc_html_e('Add','pgs-woo-api')?></button>
                    </div>
                    <div class="pgs-woo-api-panel-sidebar">
						<div class="device-select">
							<a href="javascript:void(0)" class="button button-primary android-device pgs-woo-device-img-display active" data-target="ad-banner-android-section">
								<img class="img-responsive" src="<?php echo PGS_API_URL.'img/android/android.png'?>" />
							</a>
							<a href="javascript:void(0)" class="button button-primary ios-device pgs-woo-device-img-display" data-target="ad-banner-ios-section">
								<img class="img-responsive" src="<?php echo PGS_API_URL.'img/ios/ios.png'?>" />
							</a>
						</div>
						<div id="ad-banner-android-section" class="device-display">
							<img src="<?php echo PGS_API_URL.'img/android/ad-banner.png'?>" />
						</div>
						<div id="ad-banner-ios-section" class="device-display hidden">
							<img src="<?php echo PGS_API_URL.'img/ios/ad-banner.png'?>" />
						</div>
					</div>
                </div>

            </div><!-- #pgs-woo-api-tabs-banner-ads -->
            <div id="pgs-woo-api-tabs-feature-box">
                <?php
                $feature_box_heading = (isset($pgs_woo_api_home_option['feature_box_heading']))?$pgs_woo_api_home_option['feature_box_heading']:'';
                $feature_box_status = (isset($pgs_woo_api_home_option['feature_box_status']) && !empty($pgs_woo_api_home_option['feature_box_status']))?$pgs_woo_api_home_option['feature_box_status']:'enable';
                if(!empty($lang)){
                    $pgs_woo_api_home_option_lang = get_option('pgs_woo_api_home_option_'.$lang);
                    $feature_box_heading = (isset($pgs_woo_api_home_option_lang['feature_box_heading']))?$pgs_woo_api_home_option_lang['feature_box_heading']:'';
                    $feature_box_status = (isset($pgs_woo_api_home_option_lang['feature_box_status']) && !empty($pgs_woo_api_home_option_lang['feature_box_status']))?$pgs_woo_api_home_option_lang['feature_box_status']:'enable';
                }
                ?>
                <div class="pgs-woo-api-panel" id="pgs-woo-api-feature-box">
                    <div class="pgs-woo-api-panel-body">
                    <div class="pgs-woo-api-panel-heading"><?php esc_html_e('Feature Box','pgs-woo-api')?></div>

                        <div class="pgs-woo-api-repeater-field-group" data-repeater-list="pgs[feature_box]">
                            <div class="pgs-woo-api-form-groups radio-button-inline">
                                <label><?php esc_html_e("Status",'pgs-woo-api')?></label>
                                <label><input type="radio" name="pgs[feature_box_status]" class="pgs-woo-api-form-control feature-box-status" value="enable" <?php echo ($feature_box_status == "enable")?'checked=""':'';?> /><?php esc_html_e( 'Enable','pgs-woo-api')?></label>
                                <label><input type="radio" name="pgs[feature_box_status]" class="pgs-woo-api-form-control feature-box-status" value="disable" <?php echo ($feature_box_status == "disable")?'checked=""':'';?> /><?php esc_html_e( 'Disable','pgs-woo-api')?></label>
                            </div>

                            <div class="pgs-woo-api-field-group feature-box" <?php pgs_woo_api_feature_box_status($lang)?>>
                                <div class="pgs-woo-api-form-group">
                                    <label><?php esc_html_e("Feature Box Title",'pgs-woo-api')?></label>
                                    <input type="text" name="pgs[feature_box_heading]" class="pgs-woo-api-form-control" value="<?php echo esc_attr($feature_box_heading)?>" />
                                </div>
                            </div>
                            <?php
                            $default_array['feature_box'] = array(
                                array(
                                    'feature_image_id' => '',
                                    'feature_title' => '',
                                    'feature_content' => ''
                                )
                            );
                            $pgs_feature_box = array();
                            $pgs_feature_box = $pgs_woo_api_home_option;
                            if(!empty($lang)){
                                $pgs_feature_box = get_option('pgs_woo_api_home_option_'.$lang);
                            }

                            if(empty($pgs_feature_box['feature_box'])){
                                $pgs_feature_box = $default_array;
                            }
                            if(isset($pgs_feature_box) && !empty($pgs_feature_box)){
                                foreach($pgs_feature_box['feature_box'] as $key => $val){
                                    ?>
                                    <div class="pgs-woo-api-field-group feature-box" <?php pgs_woo_api_feature_box_status($lang)?> data-repeater-item>
                                        <div class="pgs-woo-api-form-group">
                                            <label><?php esc_html_e("Feature Box Icon Image",'pgs-woo-api')?></label>
											<p><strong><?php esc_html_e('IMPORTANT','pgs-woo-api')?>:</strong><br />
												<span class="description">
												<?php
												echo wp_kses(
													__('For best performance and image quality, strictly use <strong>150 x 150</strong> (WidthxHeight in px) image for feature box.','pgs-woo-api'),
													array(
														'strong' => array()
													)
												);
												?>
												</span>
											</p>
                                            <?php
                                            if(isset($val['feature_image_id']) && !empty($val['feature_image_id'])){
                                                $src = wp_get_attachment_image_src($val['feature_image_id'], 'thumbnail' );
                                                if(!empty($src)){
                                                    ?>
                                                    <div class="upload_image">
                                                        <img src="<?php echo esc_url($src[0])?>" alt="No image" />
                                                    </div>
                                                    <input type="hidden" name="pgs[feature_box][][feature_image_id]" class="upload_image_id" value="<?php echo $val['feature_image_id']; ?>" />
                                                    <a href="javascript:void(0);" class="upload-image-button button button-default"><?php esc_html_e( 'Edit','pgs-woo-api')?></a>
                                                    <?php
                                                }
                                            } else {?>
                                                <div class="upload_image"></div>
                                                <input type="hidden" name="pgs[feature_box][][feature_image_id]" class="upload_image_id" value="" />
                                                <a href="javascript:void(0);" class="upload-image-button button button-primary"><?php esc_html_e( 'Add Image','pgs-woo-api')?></a>
                                                <?php
                                            }?>
                                            <a href="javascript:void(0);" class="remove-image-button button button-default" style="<?php echo ( ! $val['feature_image_id'] ? 'display:none;' : '' ); ?>"><?php esc_html_e( 'Remove','pgs-woo-api')?></a>
                                        </div>


                                        <div class="pgs-woo-api-form-group">
                                            <label><?php esc_html_e('Title','pgs-woo-api')?></label>
                                            <input type="text" name="pgs[feature_box][][feature_title]" class="pgs-woo-api-form-control" value="<?php echo (isset($val['feature_title']))?$val['feature_title']:''?>"  />
                                        </div>


                                        <div class="pgs-woo-api-form-group">
                                            <label><?php esc_html_e('Content','pgs-woo-api')?></label>
                                            <textarea class="pgs-woo-api-form-control" name="pgs[feature_box][][feature_content]"><?php echo (isset($val['feature_content']))?$val['feature_content']:''?></textarea>
                                        </div>
                                        <span class="removeBanner dashicons dashicons-dismiss" data-repeater-delete><!-- Remove --></span>
                                    </div>
                                    <?php
                                }?>

                                <?php
                            } ?>
                            </div>
                            <button type="button" class="pgs-woo-api-btn button button-primary feature-box" <?php pgs_woo_api_feature_box_status($lang)?> data-repeater-create><?php esc_html_e('Add','pgs-woo-api')?></button>
                        </div>

						<div class="pgs-woo-api-panel-sidebar">
							<div class="device-select">
								<a href="javascript:void(0)" class="button button-primary android-device pgs-woo-device-img-display active" data-target="featurebox-android-section">
									<img class="img-responsive" src="<?php echo PGS_API_URL.'img/android/android.png'?>" />
								</a>
								<a href="javascript:void(0)" class="button button-primary ios-device pgs-woo-device-img-display" data-target="featurebox-ios-section">
									<img class="img-responsive" src="<?php echo PGS_API_URL.'img/ios/ios.png'?>" />
								</a>
							</div>
							<div id="featurebox-android-section" class="device-display">
								<img src="<?php echo PGS_API_URL.'img/android/featurebox.png'?>" />
							</div>
							<div id="featurebox-ios-section" class="device-display hidden">
								<img src="<?php echo PGS_API_URL.'img/ios/featurebox.png'?>" />
							</div>
						</div>
                </div>
            </div><!-- #pgs-woo-api-tabs-feature-box -->

            <?php if($lang == ''){?>
                <div id="pgs-woo-api-tabs-app-pages">
                    <div class="pgs-woo-api-panel" id="pgs-woo-api-info-pages">
                        <div class="pgs-woo-api-panel-body">
                        <div class="pgs-woo-api-panel-heading"><?php esc_html_e('App Pages','pgs-woo-api')?></div>
                        <p class="description"><?php esc_html_e( 'App custom pages.','pgs-woo-api')?></p>

                            <div class="pgs-woo-api-repeater-field-group" data-repeater-list="pgs[info_pages]">
                            <?php
                            /**
                             *  Static pages sections
                             */
                            $static_page = $pgs_woo_api_home_option;
                            $about_us = (isset($static_page['static_page']['about_us']))?$static_page['static_page']['about_us']:'';
                            $terms_of_use = (isset($static_page['static_page']['terms_of_use']))?$static_page['static_page']['terms_of_use']:'';
                            $privacy_policy = (isset($static_page['static_page']['privacy_policy']))?$static_page['static_page']['privacy_policy']:'';

                            ?>
                            <div class="pgs-woo-api-form-group">
                                <label><?php esc_html_e( 'About Us','pgs-woo-api' )?></label>
                                <select class="pgs-woo-api-form-control pgs-woo-api_pages" name="pgs[static_page][about_us]">
                                    <option value=""><?php esc_html_e('Select page','pgs-woo-api')?></option>
                                    <?php
                                    $pages = get_pages();
                                    if(!empty($pages)):
                                        foreach ( $pages as $page ) {
                                            $sel = '';
                                            if( defined( 'ICL_LANGUAGE_CODE' ) && function_exists( 'icl_object_id' ) && ($lang != '') ) {
                                                $id = icl_object_id($about_us, 'page', true,$lang);
                                                $about_us = $id;
                                            }

                                            if($about_us == $page->ID){
                                                $sel = 'selected="selected"';
                                            }
                                            $option = '<option value="' . $page->ID . '" '. $sel .'>';
                                            $option .= $page->post_title;
                                            $option .= '</option>';
                                            echo $option;
                                        }
                                    endif;
                                    ?>
                                </select>

                            </div>

                            <div class="pgs-woo-api-form-group">
                                <label><?php esc_html_e( 'Terms & Conditions','pgs-woo-api' )?></label>
                                <select class="pgs-woo-api-form-control pgs-woo-api_pages" name="pgs[static_page][terms_of_use]">
                                    <option value=""><?php esc_html_e('Select page','pgs-woo-api')?></option>
                                    <?php
                                    $pages = get_pages();
                                    if(!empty($pages)):
                                        foreach ( $pages as $page ) {
                                            $sel = '';
                                            if( defined( 'ICL_LANGUAGE_CODE' ) && function_exists( 'icl_object_id' ) && ($lang != '') ) {
                                                $id = icl_object_id($terms_of_use, 'page', true,$lang);
                                                $terms_of_use = $id;
                                            }
                                            if($terms_of_use == $page->ID){
                                                $sel = 'selected="selected"';
                                            }
                                            $option = '<option value="' . $page->ID . '" '. $sel .'>';
                                            $option .= $page->post_title;
                                            $option .= '</option>';
                                            echo $option;
                                        }
                                    endif;
                                    ?>
                                </select>
                            </div>

                            <div class="pgs-woo-api-form-group">
                                <label><?php esc_html_e( 'Privacy Policy','pgs-woo-api' )?></label>
                                <select class="pgs-woo-api-form-control pgs-woo-api_pages" name="pgs[static_page][privacy_policy]">
                                    <option value=""><?php esc_html_e('Select page','pgs-woo-api')?></option>
                                    <?php
                                    $pages = get_pages();
                                    if(!empty($pages)):
                                        foreach ( $pages as $page ) {
                                            $sel = '';
                                            if( defined( 'ICL_LANGUAGE_CODE' ) && function_exists( 'icl_object_id' ) && ($lang != '') ) {
                                                $id = icl_object_id($privacy_policy, 'page', true,$lang);
                                                $privacy_policy = $id;
                                            }
                                            if($privacy_policy == $page->ID){
                                                $sel = 'selected="selected"';
                                            }
                                            $option = '<option value="' . $page->ID . '" '. $sel .'>';
                                            $option .= $page->post_title;
                                            $option .= '</option>';
                                            echo $option;
                                        }
                                    endif;
                                    ?>
                                </select>

                            </div>

                            <?php
                            /**
                             *  Dynamic info pages sections
                             */
                            $default_array['info_pages'] = array(
                                array(
                                    'info_pages_page_id' => ''
                                )
                            );
                            $info_pages = array();
                            $info_pages = $pgs_woo_api_home_option;

                            if(!isset($info_pages['info_pages']) || empty($info_pages['info_pages'])){
                                $info_pages = $default_array;
                            }

                            if(isset($info_pages['info_pages']) && !empty($info_pages['info_pages'])){
                                $i = 0;
                                foreach($info_pages['info_pages'] as $key => $val){

                                    ?>
                                    <div class="pgs-woo-api-form-group" data-repeater-item>
                                        <label><?php esc_html_e( 'Add info Pages','pgs-woo-api' )?></label>
                                        <select class="pgs-woo-api-form-control pgs-woo-api_pages" name="pgs[info_pages][<?php echo $i?>][info_pages_page_id]">
                                            <option value=""><?php esc_html_e('Select page','pgs-woo-api')?></option>
                                            <?php
                                            $pages = get_pages();
                                            if(!empty($pages)):
                                                foreach ( $pages as $page ) {
                                                    $sel = '';
                                                    if( defined( 'ICL_LANGUAGE_CODE' ) && function_exists( 'icl_object_id' ) && ($lang != '') ) {
                                                        $id = icl_object_id($val['info_pages_page_id'], 'page', true,$lang);
                                                        $val['info_pages_page_id'] = $id;
                                                    }
                                                    if($val['info_pages_page_id'] == $page->ID){
                                                        $sel = 'selected="selected"';
                                                    }
                                                    $option = '<option value="' . $page->ID . '" '. $sel .'>';
                                                    $option .= $page->post_title;
                                                    $option .= '</option>';
                                                    echo $option;
                                                }
                                            endif;
                                            ?>
                                        </select>
                                        <span class="removeBanner dashicons dashicons-dismiss" data-repeater-delete><!-- Remove --></span>
                                    </div>
                                    <?php
                                    $i++;
                                }
                            }?>
                            </div>
                            <button type="button" class="pgs-woo-api-btn button button-primary" data-repeater-create><?php esc_html_e('Add','pgs-woo-api')?></button>
                        </div>
                        <div class="pgs-woo-api-panel-sidebar">
    						<div class="device-select">
    							<a href="javascript:void(0)" class="button button-primary android-device pgs-woo-device-img-display active" data-target="pages-android-section">
    								<img class="img-responsive" src="<?php echo PGS_API_URL.'img/android/android.png'?>" />
    							</a>
    							<a href="javascript:void(0)" class="button button-primary ios-device pgs-woo-device-img-display" data-target="pages-ios-section">
    								<img class="img-responsive" src="<?php echo PGS_API_URL.'img/ios/ios.png'?>" />
    							</a>
    						</div>
    						<div id="pages-android-section" class="device-display">
    							<img src="<?php echo PGS_API_URL.'img/android/pages.png'?>" />
    						</div>
    						<div id="pages-ios-section" class="device-display hidden">
    							<img src="<?php echo PGS_API_URL.'img/ios/pages.png'?>" />
    						</div>
    					</div>
                    </div>
                </div><!-- #pgs-woo-api-tabs-app-pages -->
                <div id="pgs-woo-api-tabs-checkout-page">

                    <div class="pgs-woo-api-panel" id="pgs-woo-api-checkout-page">
                        <div class="pgs-woo-api-panel-body">
                            <div class="pgs-woo-api-panel-heading"><?php esc_html_e('Checkout Page Setting','pgs-woo-api')?></div>
                            <p class="description"><?php esc_html_e( 'Checkout page in any e-commerce website plays the most crucial part as it is the online equivalent of a physical checkout counter shown to the customer during the step by step checkout process. Hence, it needs to be appealing enough. Ciya shop comes with a feature that you can personalize this page as well. Although the user is having the default page to use they can enhance their checkout page according to their choice anytime they want by going to \'Checkout Page\' option in \'App settings\' and selecting the page they have created.','pgs-woo-api')?></p>

                            <?php
                            $is_pgs_multisteps = is_pgs_multisteps_checkout_active();
                            if($is_pgs_multisteps){
                                $pgs_woo_api_checkout_multistep = get_option('pgs_woo_api_checkout_multistep');
                                if(empty($pgs_woo_api_checkout_multistep)){
                                    $pgs_woo_api_checkout_multistep = "disable";
                                }
                                $pgs_woo_api_multi_num_of_steps = get_option('pgs_woo_api_multi_num_of_steps');
                                if(empty($pgs_woo_api_multi_num_of_steps)){
                                    $pgs_woo_api_multi_num_of_steps = 4;
                                }
                                ?>
                                <div class="pgs-woo-api-form-groups radio-button-inline">
                                    <label><?php esc_html_e("Multistep Checkout",'pgs-woo-api')?></label><br />
                                    <label><input type="radio" name="pgs_woo_api_checkout_multistep" class="pgs-woo-api-form-control checkout-multistep-button-status" value="enable" <?php echo ($pgs_woo_api_checkout_multistep == "enable")?'checked=""':'';?> /><?php esc_html_e( 'Enable','pgs-woo-api')?></label>
                                    <label><input type="radio" name="pgs_woo_api_checkout_multistep" class="pgs-woo-api-form-control checkout-multistep-button-status" value="disable" <?php echo ($pgs_woo_api_checkout_multistep == "disable")?'checked=""':'';?> /><?php esc_html_e( 'Disable','pgs-woo-api')?></label>
                                </div>

                                <div id="pgs_woo_api_multi_num_of_steps_button-groups" class="pgs-woo-api-form-groups" style="<?php echo ($pgs_woo_api_checkout_multistep=="disable")?"display:none":""?>;">
                                    <input type="radio" name="pgs_woo_api_multi_num_of_steps" value="4" id="pgs_woo_api_multi_num_of_steps-4" class="pgs-woo-api-multi-image-pick" <?php echo ($pgs_woo_api_multi_num_of_steps == 4)?'checked=""':'';?> />
                                    <label class="pgs_woo_api_multi_num_of_steps_button" for="pgs_woo_api_multi_num_of_steps-4" data-step="4">
                                        <img src="<?php echo PGS_API_URL.'img/steps-4.png'?>" alt="" width="100" height="100" id="pgs-img-steps-4" class="<?php echo ($pgs_woo_api_multi_num_of_steps == 4)?'image-select-selected':'';?>" />
                                        <span><?php esc_html_e( '4 steps checkout', 'pgs-woo-api' );?></span>
                                    </label>
                                    <input type="radio" name="pgs_woo_api_multi_num_of_steps" value="3" id="pgs_woo_api_multi_num_of_steps-3" class="pgs-woo-api-multi-image-pick" <?php echo ($pgs_woo_api_multi_num_of_steps == 3)?'checked=""':'';?>/>
                                    <label class="pgs_woo_api_multi_num_of_steps_button" for="pgs_woo_api_multi_num_of_steps-3" data-step="3">
                                        <img src="<?php echo PGS_API_URL.'img/steps-3.png'?>" alt="" width="100" height="100" id="pgs-img-steps-3" class="<?php echo ($pgs_woo_api_multi_num_of_steps == 3)?'image-select-selected':'';?>" />
                                        <span><?php esc_html_e( '3 steps checkout', 'pgs-woo-api' );?></span>
                                    </label>
                                </div>
                                <?php
                            }?>
                            <div class="pgs-woo-api-form-group">
                                <label><?php esc_html_e( 'Select Checkout Page','pgs-woo-api' )?></label>
                                <select class="pgs-woo-api-form-control pgs-woo-api_pages" name="pgs_checkout_page">
                                    <option value=""><?php echo esc_attr( __( 'Select page', 'pgs-woo-api' ) ); ?></option>
                                    <?php
                                    $checkout_page = '';
                                    $checkout_page = get_option('pgs_checkout_page');
                                    $pages = get_pages();
                                    foreach ( $pages as $page ) {
                                        $selected = '';
                                        if(isset($checkout_page) && !empty($checkout_page)){
                                            if($checkout_page == $page->ID){
                                                $selected = 'selected';
                                            }
                                        } else {
                                            $is_page = pgs_woo_api_get_page_title_for_slug('my-account');
                                            if($is_page){
                                                if($is_page->ID == $page->ID){
                                                    $selected = 'selected';
                                                }
                                            }
                                        }
                                        $option = '<option value="' . $page->ID . '" '. $selected .'>';
                                        $option .= $page->post_title;
                                        $option .= '</option>';
                                        echo $option;
                                    }
                                    ?>
                                </select>
                            </div>

                            <!--Payment Gateway redirect URLs Start -->
                            <div class="pgs-woo-api-form-group">
                                <label><?php esc_html_e( 'Custom Redirect URL(s)','pgs-woo-api' )?></label>
                                <p class="description"><?php esc_html_e( 'Please add custom redirect url(s) you set in payment getway(s). Leave blank if you did not set. For example, your payment gateway redirect URL is.','pgs-woo-api')?></p>
                                <p class="description">i.e http://exampledomain.com/thankyou. then add <strong>/thankyou/</strong></p>
                                <p class="description"><strong><?php esc_html_e( 'Separate each entry with the new line.','pgs-woo-api')?></strong></p>
                            </div>
                            <?php
                            $pgs_woo_api_checkout_custom_redirect_urls = get_option('pgs_woo_api_checkout_custom_redirect_urls');
                            $custom_url = '';
                            if(isset($pgs_woo_api_checkout_custom_redirect_urls)){
                                $custom_url = $pgs_woo_api_checkout_custom_redirect_urls;
                            }
                            ?>
                            <div class="pgs-woo-api-form-group">
                                <textarea id="pgs_woo_api_custom_url_editor_field" class="pgs-woo-api-form-control" name="pgs_woo_api_checkout_custom_redirect_urls" placeholder="i.e /thankyou/"><?php echo htmlspecialchars( $custom_url );?></textarea>
                            </div>
                            <!--Payment Gateway redirect URLs End -->

                            <div class="pgs-woo-api-form-group">
                                <label><?php esc_html_e( 'Custom Css','pgs-woo-api' )?></label>
                                <p class="description"><?php esc_html_e( 'You can add custom css code for app checkout page here.','pgs-woo-api')?></p>
                            </div>
                            <?php
                            $pgs_woo_api_checkout_custom_css = get_option('pgs_woo_api_checkout_custom_css');
                            $custom_css = '';
                            if(isset($pgs_woo_api_checkout_custom_css)){
                                $custom_css = $pgs_woo_api_checkout_custom_css;
                            }
                            ?>
                            <div class="pgs-woo-api-form-group">
                                <input id="pgs_woo_api_custom_css_editor_field" name="pgs_woo_api_checkout_custom_css" type="hidden" value="<?php echo htmlspecialchars( $custom_css );?>" style="display: none;">
                                <pre id="pgs_woo_api_custom_css_editor"><?php echo htmlspecialchars( $custom_css );?></pre>
                            </div>
                        </div>

                        <div class="pgs-woo-api-panel-sidebar">
							<div class="device-select">
								<a href="javascript:void(0)" class="button button-primary android-device pgs-woo-device-img-display active" data-target="checkout-android-section">
									<img class="img-responsive" src="<?php echo PGS_API_URL.'img/android/android.png'?>" />
								</a>
								<a href="javascript:void(0)" class="button button-primary ios-device pgs-woo-device-img-display" data-target="checkout-ios-section">
									<img class="img-responsive" src="<?php echo PGS_API_URL.'img/ios/ios.png'?>" />
								</a>
							</div>
							<div id="checkout-android-section" class="device-display">
                                <?php
                                $checkout_img = 'checkout.png';
                                if( isset($pgs_woo_api_checkout_multistep) && $pgs_woo_api_checkout_multistep == "enable" ){
                                    $checkout_img = 'multistep.png';
                                }?>
                                <img src="<?php echo PGS_API_URL.'img/android/'.$checkout_img?>" />
							</div>
							<div id="checkout-ios-section" class="device-display hidden">
								<img src="<?php echo PGS_API_URL.'img/ios/'.$checkout_img?>" />
							</div>
						</div>
                    </div>

              </div>

            <div id="pgs-woo-api-tabs-social-links">

                    <div class="pgs-woo-api-panel" id="">
                        <div class="pgs-woo-api-panel-body">
                            <div class="pgs-woo-api-panel-heading"><?php esc_html_e('Social Media Settings','pgs-woo-api')?></div>
                            <p class="description"><?php esc_html_e( 'Specify social media URLs, where the user redirected once he/she clicks on social media icons.','pgs-woo-api')?></p>

                            <?php
                            $facebook = (isset($pgs_woo_api_home_option['pgs_app_social_links']['facebook']))?$pgs_woo_api_home_option['pgs_app_social_links']['facebook']:'';
                            $twitter = (isset($pgs_woo_api_home_option['pgs_app_social_links']['twitter']))?$pgs_woo_api_home_option['pgs_app_social_links']['twitter']:'';
                            $linkedin = (isset($pgs_woo_api_home_option['pgs_app_social_links']['linkedin']))?$pgs_woo_api_home_option['pgs_app_social_links']['linkedin']:'';
                            $google_plus = (isset($pgs_woo_api_home_option['pgs_app_social_links']['google_plus']))?$pgs_woo_api_home_option['pgs_app_social_links']['google_plus']:'';
                            $pinterest = (isset($pgs_woo_api_home_option['pgs_app_social_links']['pinterest']))?$pgs_woo_api_home_option['pgs_app_social_links']['pinterest']:'';
                            $instagram = (isset($pgs_woo_api_home_option['pgs_app_social_links']['instagram']))?$pgs_woo_api_home_option['pgs_app_social_links']['instagram']:'';
                            ?>
                            <div class="pgs-woo-api-field-groups">
                                <div class="pgs-woo-api-form-groups">
                                    <label><?php esc_html_e("Facebook",'pgs-woo-api')?></label>
                                    <input type="text" name="pgs[pgs_app_social_links][facebook]" class="pgs-woo-api-form-control" value="<?php echo esc_attr($facebook)?>" />
                                </div>
                                <div class="pgs-woo-api-form-groups">
                                    <label><?php esc_html_e("Twitter",'pgs-woo-api')?></label>
                                    <input type="text" name="pgs[pgs_app_social_links][twitter]" class="pgs-woo-api-form-control" value="<?php echo esc_attr($twitter)?>" />
                                </div>

                                <div class="pgs-woo-api-form-groups">
                                    <label><?php esc_html_e("LinkedIn",'pgs-woo-api')?></label>
                                    <input type="text" name="pgs[pgs_app_social_links][linkedin]" class="pgs-woo-api-form-control" value="<?php echo esc_attr($linkedin)?>" />
                                </div>
                                <div class="pgs-woo-api-form-groups">
                                    <label><?php esc_html_e("Google Plus",'pgs-woo-api')?></label>
                                    <input type="text" name="pgs[pgs_app_social_links][google_plus]" class="pgs-woo-api-form-control" value="<?php echo esc_attr($google_plus)?>" />
                                </div>
                                <div class="pgs-woo-api-form-groups">
                                    <label><?php esc_html_e("Pinterest",'pgs-woo-api')?></label>
                                    <input type="text" name="pgs[pgs_app_social_links][pinterest]" class="pgs-woo-api-form-control" value="<?php echo esc_attr($pinterest)?>" />
                                </div>

                                <div class="pgs-woo-api-form-groups">
                                    <label><?php esc_html_e("Instagram",'pgs-woo-api')?></label>
                                    <input type="text" name="pgs[pgs_app_social_links][instagram]" class="pgs-woo-api-form-control" value="<?php echo esc_attr($instagram)?>" />
                                </div>
                            </div>
                        </div>

                       <div class="pgs-woo-api-panel-sidebar">
							<div class="device-select">
								<a href="javascript:void(0)" class="button button-primary android-device pgs-woo-device-img-display active" data-target="social-links-android-section">
									<img class="img-responsive" src="<?php echo PGS_API_URL.'img/android/android.png'?>" />
								</a>
								<a href="javascript:void(0)" class="button button-primary ios-device pgs-woo-device-img-display" data-target="social-links-ios-section">
									<img class="img-responsive" src="<?php echo PGS_API_URL.'img/ios/ios.png'?>" />
								</a>
							</div>
							<div id="social-links-android-section" class="device-display">
								<img src="<?php echo PGS_API_URL.'img/android/social-links.png'?>" />
							</div>
							<div id="social-links-ios-section" class="device-display hidden">
								<img src="<?php echo PGS_API_URL.'img/ios/social-links.png'?>" />
							</div>
						</div>
                    </div>

            </div><!-- #pgs-woo-api-tabs-social-links -->

            <div id="pgs-woo-api-tabs-contact-info">

                    <div class="pgs-woo-api-panel" id="">
                        <div class="pgs-woo-api-panel-body">
                            <div class="pgs-woo-api-panel-heading"><?php esc_html_e('Contact Info','pgs-woo-api')?></div>
                            <?php
                            $address_line_1 = (isset($pgs_woo_api_home_option['pgs_app_contact_info']['address_line_1']))?$pgs_woo_api_home_option['pgs_app_contact_info']['address_line_1']:'';
                            $address_line_2 = (isset($pgs_woo_api_home_option['pgs_app_contact_info']['address_line_2']))?$pgs_woo_api_home_option['pgs_app_contact_info']['address_line_2']:'';
                            $email = (isset($pgs_woo_api_home_option['pgs_app_contact_info']['email']))?$pgs_woo_api_home_option['pgs_app_contact_info']['email']:'';
                            $phone = (isset($pgs_woo_api_home_option['pgs_app_contact_info']['phone']))?$pgs_woo_api_home_option['pgs_app_contact_info']['phone']:'';
                            ?>
                            <div class="pgs-woo-api-field-groups">
                                <div class="pgs-woo-api-form-groups">
                                    <label><?php esc_html_e("Address Line 1",'pgs-woo-api')?></label>
                                    <input type="text" name="pgs[pgs_app_contact_info][address_line_1]" class="pgs-woo-api-form-control" value="<?php echo esc_attr($address_line_1)?>" />
                                </div>
                                <div class="pgs-woo-api-form-groups">
                                    <label><?php esc_html_e("Address Line 2",'pgs-woo-api')?></label>
                                    <input type="text" name="pgs[pgs_app_contact_info][address_line_2]" class="pgs-woo-api-form-control" value="<?php echo esc_attr($address_line_2)?>" />
                                </div>

                                <div class="pgs-woo-api-form-groups">
                                    <label><?php esc_html_e("Email",'pgs-woo-api')?></label>
                                    <input type="text" name="pgs[pgs_app_contact_info][email]" class="pgs-woo-api-form-control" value="<?php echo esc_attr($email)?>" />
                                </div>
                                <div class="pgs-woo-api-form-groups">
                                    <label><?php esc_html_e("Phone",'pgs-woo-api')?></label>
                                    <input type="text" name="pgs[pgs_app_contact_info][phone]" class="pgs-woo-api-form-control" value="<?php echo esc_attr($phone)?>" />
                                </div>
                            </div>
                        </div>

                       <div class="pgs-woo-api-panel-sidebar">
							<div class="device-select">
								<a href="javascript:void(0)" class="button button-primary android-device pgs-woo-device-img-display active" data-target="contact-info-android-section">
									<img class="img-responsive" src="<?php echo PGS_API_URL.'img/android/android.png'?>" />
								</a>
								<a href="javascript:void(0)" class="button button-primary ios-device pgs-woo-device-img-display" data-target="contact-info-ios-section">
									<img class="img-responsive" src="<?php echo PGS_API_URL.'img/ios/ios.png'?>" />
								</a>
							</div>
							<div id="contact-info-android-section" class="device-display">
								<img src="<?php echo PGS_API_URL.'img/android/contact-info.png'?>" />
							</div>
							<div id="contact-info-ios-section" class="device-display hidden">
								<img src="<?php echo PGS_API_URL.'img/ios/contact-info.png'?>" />
							</div>
						</div>
                    </div>

            </div><!-- #pgs-woo-api-tabs-contact-info -->

            <div id="pgs-woo-api-tabs-whatsapp">

                    <div class="pgs-woo-api-panel" id="">
                        <div class="pgs-woo-api-panel-body">
                            <div class="pgs-woo-api-panel-heading"><?php esc_html_e('WhatsApp','pgs-woo-api')?></div>
                            <?php
                            $whatsapp_floating_button = 'disable';
                            if(isset($pgs_woo_api_home_option['pgs_app_contact_info']['whatsapp_floating_button'])){
                                $whatsapp_floating_button = $pgs_woo_api_home_option['pgs_app_contact_info']['whatsapp_floating_button'];
                            }
                            $whatsapp_no = $pgs_woo_api_home_option['pgs_app_contact_info']['whatsapp_no'];
                            $whatsappno = (isset($whatsapp_no) && !empty($whatsapp_no))?esc_attr($whatsapp_no):'';
                            if(empty($whatsappno)){
                                $whatsapp_floating_button = 'disable';
                            }?>
                            <div class="pgs-woo-api-field-group pgs-woo-api-whatsapp-no">
                                <p class="description"><?php esc_html_e( 'Enter WhatsApp number with your country code, e.g. India +91 with your WhatsApp number like +911234567890','pgs-woo-api')?></p>
                                <div class="pgs-woo-api-form-groups">
                                    <label><?php esc_html_e("WhatsApp No",'pgs-woo-api')?></label>
                                    <input id="pgs-woo-api-whatsapp-no-validation" type="text" name="pgs[pgs_app_contact_info][whatsapp_no]" class="pgs-woo-api-form-control" value="<?php echo $whatsappno?>" placeholder="e.g. +911234567890" />
                                    <span id="validation_status"></span>
                                </div>
                            </div>
                            <?php if($whatsappno != ''){?>
                            <div class="pgs-woo-api-form-groups radio-button-inline">
                                <label><?php esc_html_e("WhatsApp Floating Button",'pgs-woo-api')?></label><br />
                                <label><input type="radio" name="pgs[pgs_app_contact_info][whatsapp_floating_button]" class="pgs-woo-api-form-control whatsapp-floating-button-status" value="enable" <?php echo ($whatsapp_floating_button == "enable")?'checked=""':'';?> /><?php esc_html_e( 'Enable','pgs-woo-api')?></label>
                                <label><input type="radio" name="pgs[pgs_app_contact_info][whatsapp_floating_button]" class="pgs-woo-api-form-control whatsapp-floating-button-status" value="disable" <?php echo ($whatsapp_floating_button == "disable")?'checked=""':'';?> /><?php esc_html_e( 'Disable','pgs-woo-api')?></label>
                            </div>
                            <?php }?>
                        </div>

                       <div class="pgs-woo-api-panel-sidebar">
							<div class="device-select">
								<a href="javascript:void(0)" class="button button-primary android-device pgs-woo-device-img-display active" data-target="whatsapp-float-android-section">
									<img class="img-responsive" src="<?php echo PGS_API_URL.'img/android/android.png'?>" />
								</a>
								<a href="javascript:void(0)" class="button button-primary ios-device pgs-woo-device-img-display" data-target="whatsapp-float-ios-section">
									<img class="img-responsive" src="<?php echo PGS_API_URL.'img/ios/ios.png'?>" />
								</a>
							</div>
							<div id="whatsapp-float-android-section" class="device-display">
								<img src="<?php echo PGS_API_URL.'img/android/whatsapp_float.png'?>" />
							</div>
							<div id="whatsapp-float-ios-section" class="device-display hidden">
								<img src="<?php echo PGS_API_URL.'img/ios/whatsapp_float.png'?>" />
							</div>
						</div>
                    </div>

            </div><!-- #pgs-woo-api-tabs-whatsapp -->
            <?php }?>

            <?php if($lang == ''){?>
                <div id="pgs-woo-api-tabs-sample-data">
                    <div class="pgs-woo-api-panel" id="pgs-woo-api-sample-data">
                        <div class="pgs-woo-api-panel-body">
                            <div id="pgs-woo-api-panel-body-loader"></div>
                            <div class="pgs-woo-api-panel-heading"><?php esc_html_e('Sample Data','pgs-woo-api')?></div>
                            <div class="pgs-woo-api-repeater-field-group">
                                <p class="description"><?php esc_html_e('You can import pre-defined sample data, as shown on our demo site, from here.','pgs-woo-api')?></p>
                                <p class="description"><?php esc_html_e('Please take a backup before importing any sample data to prevent any data loss during installation.','pgs-woo-api')?></p>
                                <div class="data-alert-notitication"></div>
                                <div id="pgs-expand-div-options" class="pgs-woo-api-sort-sample-data">
                                    <?php
                                    $sample_datas = pgs_woo_api_plugin_sample_datas();
                                    if( !pgs_woo_api_token_is_activated() ){
                    					$sample_datas = array();
                    				}
                                    $nonce = wp_create_nonce( "pgs_woo_api_sample_data_security");
                                    if( !empty($sample_datas) && is_array($sample_datas) ){

                        				$sample_data_path = PGS_API_PATH.'inc/sample_data';
                        				$sample_data_url  = PGS_API_URL.'inc/sample_data';
                        				?>
                        				<div class="sample-data-items">
                        					<?php
                        					$imported_samples = array();
                                            $pgs_woo_api_sample_data_arr = get_option( 'pgs_woo_api_default_sample_data_arr' );
                        					if(isset($pgs_woo_api_sample_data_arr) && !empty($pgs_woo_api_sample_data_arr)){
                                                $imported_samples = json_decode($pgs_woo_api_sample_data_arr);
                                            }
                                            $sample_data_active = get_option( 'pgs_woo_api_default_sample_data_active' );
                                            foreach( $sample_datas as $sample_data ){
                        						$sample_data_id = sanitize_title($sample_data['id']);
                                                //Hide already install sample data
                                                $sample_data_item_classes_array = array(
                        							'sample-data-item sample-data-item-'.$sample_data_id
                        						);
                        						$sample_data_item_classes = implode( ' ', array_filter( array_unique( $sample_data_item_classes_array ) ) );


                                                $preview_img_path = trailingslashit(trailingslashit($sample_data_path).$sample_data['id']).'preview.jpg';
                        						$preview_img_url = trailingslashit(trailingslashit($sample_data_url).$sample_data['id']).'preview.jpg';

                                                $html = '';$check_icon='';
                                                if(!empty($imported_samples) && in_array($sample_data_id, $imported_samples)){
                                                    if($sample_data_active == $sample_data_id){
                                                        $html = '<i class="fa fa-check"></i>';
                                                        $check_icon = '<span class="dashicons dashicons-yes"></span>';
                                                    }
                                                }

                                                ?>
                        						<div class="<?php echo esc_attr($sample_data_item_classes);?>">
                        							<?php
                        							if( file_exists($preview_img_path) ){
                        								?>
                        								<div class="sample-data-item-screenshot">
                        									<img src="<?php echo esc_url($preview_img_url);?>" alt="<?php echo esc_attr($sample_data['name']);?>"/>
                        								</div>
                        								<?php
                        							}else{
                        								?>
                        								<div class="sample-data-item-screenshot blank"></div>
                        								<?php
                        							}
                        							?>
                        							<span class="sample-data-item-details"><?php echo esc_html($sample_data['name']);?></span>
                        							<h2 class="sample-data-item-name"><?php echo esc_html($sample_data['name']);?><?php echo $check_icon?></h2>
                        							<div class="sample-data-item-actions">
                        								<?php $required_plugins_list = pgs_woo_api_sample_data_required_plugins_list();?>
                        								<button class="button button-primary pgs-woo-api-import-this-sample hide-if-no-customize"
                        									data-id="<?php echo esc_attr($sample_data['id']);?>"
                        									data-nonce="<?php echo esc_attr($nonce);?>"
                        									data-title="<?php echo esc_attr($sample_data['name']);?>"
                        									data-title="<?php echo esc_attr($sample_data['name']);?>"
                        									data-message="<?php echo esc_attr($sample_data['message']); ?>"
                                                            <?php echo ( !empty($required_plugins_list) ) ? 'data-required-plugins="'.esc_attr(count($required_plugins_list)).'"' : '';?>>
                        									<?php echo esc_html__('Install', 'pgs-woo-api');?>
                        								</button>
                        							</div>
                        						</div>
                        						<?php
                        					}
                        					?>
                        				</div>
                        				<?php
                        			}?>

                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <div id="pgs-woo-api-tabs-is-wpml">
                    <div class="pgs-woo-api-panel" id="pgs-woo-api-is-wpml">
                        <div class="pgs-woo-api-panel-body">
                            <div class="pgs-woo-api-panel-heading"><?php esc_html_e('WPML','pgs-woo-api')?></div>
                            <div class="pgs-woo-api-repeater-field-group">
                                <div class="pgs-woo-api-form-groups radio-button-inline">
                                    <?php
                                    $pgs_api_is_wpml_status = (isset($pgs_woo_api_home_option['pgs_api_is_wpml']))?$pgs_woo_api_home_option['pgs_api_is_wpml']:'enable';
                                    ?>
                                    <label><?php esc_html_e("Status",'pgs-woo-api')?></label>
                                    <label><input type="radio" name="pgs[pgs_api_is_wpml]" class="pgs-woo-api-form-control feature-box-status" value="enable" <?php echo ($pgs_api_is_wpml_status == "enable")?'checked=""':'';?> /><?php esc_html_e( 'Enable','pgs-woo-api')?></label>
                                    <label><input type="radio" name="pgs[pgs_api_is_wpml]" class="pgs-woo-api-form-control feature-box-status" value="disable" <?php echo ($pgs_api_is_wpml_status == "disable")?'checked=""':'';?> /><?php esc_html_e( 'Disable','pgs-woo-api')?></label>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
				<div id="pgs-woo-api-custom-section-layout">

                 <div class="pgs-woo-api-panel" id="pgs-woo-api-category-banners">
                    <div class="pgs-woo-api-panel-body">
                    <div class="pgs-woo-api-panel-heading"><?php esc_html_e('Custom Section','pgs-woo-api')?></div>
                    <p class="description"><?php esc_html_e( 'Custom Section displayed on the home screen.','pgs-woo-api' )?></p>
					<div class="pgs-woo-api-repeater-field-group" data-repeater-list="pgs[custom_section]">
                        <?php
                        $default_array['custom_section'] = array(
                            array(
                                'product_banners_cat_id' => ''
                            )
                        );
                        $pgs_custom_section = array();
                        $pgs_custom_section = $pgs_woo_api_home_option;

                        if(!empty($lang)){
                            $pgs_custom_section = get_option('pgs_woo_api_home_option_'.$lang);
                        }

                        if(empty($pgs_custom_section['custom_section'])){
                            $pgs_custom_section = $default_array;
                        }
                        if(isset($pgs_custom_section['custom_section']) && !empty($pgs_custom_section['custom_section'])){
                            ?>
                                <div class="pgs-woo-api-field-group" >
                                    <div class="pgs-woo-api-form-groups radio-button-inline">
										<?php
										$pgs_api_is_wpml_status = (isset($pgs_custom_section['product_banners_cat_value']))?$pgs_custom_section['product_banners_cat_value']:'enable';
										?>
										<label><?php esc_html_e("Custom Section Option",'pgs-woo-api')?></label>
										<label><input type="radio" name="pgs[product_banners_cat_value]" class="pgs-woo-api-form-control custom-section-box-status" value="enable" <?php echo ($pgs_api_is_wpml_status == "enable")?'checked=""':'';?> /><?php esc_html_e( 'Enable','pgs-woo-api')?></label>
										<label><input type="radio" name="pgs[product_banners_cat_value]" class="pgs-woo-api-form-control custom-section-box-status" value="disable" <?php echo ($pgs_api_is_wpml_status == "disable")?'checked=""':'';?> /><?php esc_html_e( 'Disable','pgs-woo-api')?></label>
                                    </div>

                                    <div class="pgs-woo-api-form-group" id="pgs-woo-api-custom-section-title">
                                        <label><?php esc_html_e('Title','pgs-woo-api')?></label>
                                        <input type="text" name="pgs[product_banners_title]" class="pgs-woo-api-form-control" value="<?php echo isset( $pgs_custom_section['product_banners_title'] ) ? $pgs_custom_section['product_banners_title'] : ''; ?>"  />
                                    </div>
									<div id="pgs-woo-api-custom-section-product"  >
										<?php
										$i = 0;
										foreach($pgs_custom_section['custom_section'] as $key => $val){
                                            $selected_title = ( isset($val['product_banners_cat_id']) && ! empty( $val['product_banners_cat_id'] ) ) ? get_the_title($val['product_banners_cat_id']) : '';
                                            ?>
                                            <div class="pgs-woo-api-field-group  pgs-woo-api-custom-section-product" data-repeater-item>
                                                <div class="pgs-woo-api-form-group" >
                                                    <label><?php esc_html_e('Select Product','pgs-woo-api')?></label>
                                                    <select class="pgs-woo-api-form-control pgs-woo-api_pages pgs-custom-section-select2" name="pgs[custom_section][<?php echo $i?>][product_banners_cat_id]">
                                                        <option value=""><?php esc_html_e('Select Product','pgs-woo-api')?></option>
                                                        <option value="<?php echo $val['product_banners_cat_id']?>" selected="selected"><?php echo $selected_title; ?></option>
                                                    </select>
                                                </div>
                                                <span class="removeBanner pgsremovecfield dashicons dashicons-dismiss" data-repeater-delete><!-- Remove --></span>
                                            </div>
                                            <?php
											$i++;
										}
										?>
									</div>
                                </div>
                               <?php
                        } ?>

                        </div>
                        <button type="button" class="pgs-woo-api-btn pgsaddcfield button button-primary" data-repeater-create><?php esc_html_e('Add','pgs-woo-api')?></button>
                    </div>
                    <div class="pgs-woo-api-panel-sidebar">
						<div class="device-select">
							<a href="javascript:void(0)" class="button button-primary android-device pgs-woo-device-img-display active" data-target="custom-section-android-section">
								<img class="img-responsive" src="<?php echo PGS_API_URL.'img/android/android.png'?>" />
							</a>
							<a href="javascript:void(0)" class="button button-primary ios-device pgs-woo-device-img-display" data-target="custom-section-ios-section">
								<img class="img-responsive" src="<?php echo PGS_API_URL.'img/ios/ios.png'?>" />
							</a>
						</div>
						<div id="custom-section-android-section" class="device-display">
							<img src="<?php echo PGS_API_URL.'img/android/custom-section.png'?>" />
						</div>
						<div id="custom-section-ios-section" class="device-display hidden">
							<img src="<?php echo PGS_API_URL.'img/ios/custom-section.png'?>" />
						</div>
					</div>
                </div>
              </div><!-- #pgs-woo-api-custom-section-layout -->
			  <?php /*?>
			    <div id="pgs-woo-api-tabs-is-web-view">
                    <div class="pgs-woo-api-panel" id="pgs-woo-api-is-web-view">
                        <div class="pgs-woo-api-panel-body">
                            <div class="pgs-woo-api-panel-heading"><?php esc_html_e('Web View','pgs-woo-api')?></div>
								<div class="pgs-woo-api-repeater-field-group" data-repeater-list="pgs[web_view_pages]">
								<?php
								/**
								 *  Dynamic web view pages sections
								 * /
								$default_array['web_view_pages'] = array(
									array(
										'web_view_pages_page_id' => ''
									)
								);
								$web_view_pages = array();
								$web_view_pages = $pgs_woo_api_home_option;

								if(!isset($web_view_pages['web_view_pages']) || empty($web_view_pages['web_view_pages'])){
									$web_view_pages = $default_array;
								}

								if(isset($web_view_pages['web_view_pages']) && !empty($web_view_pages['web_view_pages'])){
									$i = 0;
									foreach($web_view_pages['web_view_pages'] as $key => $val){

										?>
										<div class="pgs-woo-api-form-group" data-repeater-item>
											<label><?php esc_html_e( 'Add web view Page Title','pgs-woo-api' )?></label>
											<input value="<?php echo $val['web_view_pages_page_title'];?>" class="pgs-woo-api-form-control" type="text" name="pgs[web_view_pages][<?php echo $i?>][web_view_pages_page_title]" />
											<label><?php esc_html_e( 'Add web view Pages','pgs-woo-api' )?></label>
											<select class="pgs-woo-api-form-control pgs-woo-api_pages" name="pgs[web_view_pages][<?php echo $i?>][web_view_pages_page_id]">
												<option value=""><?php esc_html_e('Select page','pgs-woo-api')?></option>
												<?php
												$pages = get_pages();
												if(!empty($pages)):
													foreach ( $pages as $page ) {
														$sel = '';
														if( defined( 'ICL_LANGUAGE_CODE' ) && function_exists( 'icl_object_id' ) && ($lang != '') ) {
															$id = icl_object_id($val['web_view_pages_page_id'], 'page', true,$lang);
															$val['web_view_pages_page_id'] = $id;
														}
														if($val['web_view_pages_page_id'] == $page->ID){
															$sel = 'selected="selected"';
														}
														$option = '<option value="' . $page->ID . '" '. $sel .'>';
														$option .= $page->post_title;
														$option .= '</option>';
														echo $option;
													}
												endif;
												?>
											</select>
											<span class="removeBanner dashicons dashicons-dismiss" data-repeater-delete></span>
										</div>
										<?php
										$i++;
									}
								}?>
							</div>
							<button type="button" class="pgs-woo-api-btn button button-primary" data-repeater-create><?php esc_html_e('Add','pgs-woo-api')?></button>
                        </div>
                    </div>
                </div>
				<?php */?>

                <div id="pgs-woo-api-tabs-filters">
                    <div class="pgs-woo-api-panel" id="pgs-woo-api-filters">
                        <div class="pgs-woo-api-panel-body">
                            <div class="pgs-woo-api-panel-heading"><?php esc_html_e('Filters','pgs-woo-api')?></div>
                            <div class="pgs-woo-api-repeater-field-group">
                                    <?php
                                    $pgs_price_filters = (isset($pgs_woo_api_home_option['pgs_api_filters']['pgs_price']))?$pgs_woo_api_home_option['pgs_api_filters']['pgs_price']:'enable';
                                    $pgs_average_rating = (isset($pgs_woo_api_home_option['pgs_api_filters']['pgs_average_rating']))?$pgs_woo_api_home_option['pgs_api_filters']['pgs_average_rating']:'enable';
                                    ?>
                                    <div class="pgs-woo-api-form-groups radio-button-inline">
                                        <label><?php esc_html_e("Price",'pgs-woo-api')?></label><br />
                                        <label><input type="radio" name="pgs[pgs_api_filters][pgs_price]" class="pgs-woo-api-form-control" value="enable"  <?php echo ($pgs_price_filters == "enable")?'checked=""':'';?> /><?php esc_html_e( 'Enable','pgs-woo-api')?></label>
                                        <label><input type="radio" name="pgs[pgs_api_filters][pgs_price]" class="pgs-woo-api-form-control" value="disable" <?php echo ($pgs_price_filters == "disable")?'checked=""':'';?> /><?php esc_html_e( 'Disable','pgs-woo-api')?></label>
                                    </div>
                                    <?php
                                    $attribute_taxonomies = wc_get_attribute_taxonomies();
                                    if ( ! empty( $attribute_taxonomies ) ) {
                        				foreach ( $attribute_taxonomies as $tax ) {
                                            $attribute    = wc_sanitize_taxonomy_name( $tax->attribute_name );
                        					$taxonomy     = wc_attribute_taxonomy_name( $attribute );
                                            $pgs_taxonomy_filters = (isset($pgs_woo_api_home_option['pgs_api_filters'][$taxonomy]))?$pgs_woo_api_home_option['pgs_api_filters'][$taxonomy]:'enable';
                                            ?>
                                            <div class="pgs-woo-api-form-groups radio-button-inline">
                                                <label><?php echo ucfirst($tax->attribute_label)?></label><br />
                                                <label><input type="radio" name="pgs[pgs_api_filters][<?php echo $taxonomy?>]" class="pgs-woo-api-form-control" value="enable" <?php echo ($pgs_taxonomy_filters == "enable")?'checked=""':'';?> /><?php esc_html_e( 'Enable','pgs-woo-api')?></label>
                                                <label><input type="radio" name="pgs[pgs_api_filters][<?php echo $taxonomy?>]" class="pgs-woo-api-form-control" value="disable" <?php echo ($pgs_taxonomy_filters == "disable")?'checked=""':'';?> /><?php esc_html_e( 'Disable','pgs-woo-api')?></label>
                                            </div>
                                            <?php
                                        }
                        			}
                                    ?>
                                    <div class="pgs-woo-api-form-groups radio-button-inline">
                                        <label><?php esc_html_e("Average Rating",'pgs-woo-api')?></label><br />
                                        <label><input type="radio" name="pgs[pgs_api_filters][pgs_average_rating]" class="pgs-woo-api-form-control" value="enable" <?php echo ($pgs_average_rating == "enable")?'checked=""':'';?>/><?php esc_html_e( 'Enable','pgs-woo-api')?></label>
                                        <label><input type="radio" name="pgs[pgs_api_filters][pgs_average_rating]" class="pgs-woo-api-form-control" value="disable" <?php echo ($pgs_average_rating == "disable")?'checked=""':'';?> /><?php esc_html_e( 'Disable','pgs-woo-api')?></label>
                                    </div>
                            </div>
                        </div>
                    </div>
                </div>

				<div id="pgs-woo-api-tabs-wallet">
					<div class="pgs-woo-api-panel" id="pgs-woo-api-wallet">
						<div class="pgs-woo-api-panel-body">
							<div class="pgs-woo-api-panel-heading"><?php esc_html_e('Wallet','pgs-woo-api')?></div>
							<p class="description"><?php esc_html_e( 'From this section, you can manage settings related to the wallet.', 'pgs-woo-api'); ?></p>
							<div class="pgs-woo-api-form-group">
                                <label><?php esc_html_e( 'Wallet Topup Page','pgs-woo-api' )?></label>
                                <select class="pgs-woo-api-form-control pgs-woo-api_pages" name="_pgs_woo_api_woowallet_topup_page">
									<option value=""><?php echo esc_html__( 'Select page', 'pgs-woo-api' ); ?></option>
                                    <?php
                                    $checkout_page = '';
                                    $checkout_page = get_option('_pgs_woo_api_woowallet_topup_page');
                                    $pages = get_pages();
                                    foreach ( $pages as $page ) {
                                        $selected = '';
                                        if(isset($checkout_page) && !empty($checkout_page)){
                                            if($checkout_page == $page->ID){
                                                $selected = 'selected';
                                            }
                                        } else {
                                            $is_page = pgs_woo_api_get_page_title_for_slug('my-account');
                                            if($is_page){
                                                if($is_page->ID == $page->ID){
                                                    $selected = 'selected';
                                                }
                                            }
                                        }
                                        $option = '<option value="' . $page->ID . '" '. $selected .'>';
                                        $option .= $page->post_title;
                                        $option .= '</option>';
                                        echo $option;
                                    }
                                    ?>
                                </select>
                            </div>
						</div>
					</div>
				</div>
				<div id="pgs-woo-api-tabs-delete-account">
					<div class="pgs-woo-api-panel" id="pgs-woo-api-delete-account">
						<?php
						$wc_settings_link = add_query_arg( array(
							'page' => 'wc-settings',
							'tab'  => 'account',
						), admin_url( 'admin.php' ) );

						$delete_account_alert_title   = pgs_woo_api_delete_account_alert_title();
						$delete_account_alert_message = pgs_woo_api_delete_account_alert_message();
						?>
						<div class="pgs-woo-api-panel-body">
							<div class="pgs-woo-api-panel-heading"><?php esc_html_e( 'Delete Account', 'pgs-woo-api' ); ?></div>
							<p class="description"><?php esc_html_e( 'From this section, you can manage settings related to deleting a user account.', 'pgs-woo-api'); ?></p>
							<h2><?php esc_html_e( 'WooCommerce Data Removal', 'pgs-woo-api' ); ?></h2>
							<div class="pgs-woo-api-panel-woocommerce-erasure-request-notice">
								<p><?php echo esc_html__( 'If you want to delete the WooCommerce order and download data along with the account deletion, then you have to configure settings in WooCommerce.', 'pgs-woo-api'); ?></p>
								<p><?php echo sprintf( wp_kses( __( 'For this, go to <strong>Admin Panel > WooCommerce > Settings > Accounts & Privacy </strong> or <a href="%s">click here</a>, scroll to the <strong>Account erasure requests</strong> field group and select the below fields.', 'pgs-woo-api' ), array( 'strong' => array(), 'a' => array( 'href' => true ) ) ), $wc_settings_link ); ?></p>
								<div class="help-tab-content active">
									<ul>
										<li><?php esc_html_e( 'Remove personal data from orders on request', 'pgs-woo-api'); ?></li>
										<li><?php esc_html_e( 'Remove access to downloads on request', 'pgs-woo-api'); ?></li>
									</ul>
								</div>
							</div>
							<h2><?php esc_html_e( 'Delete Account Message', 'pgs-woo-api'); ?></h2>
							<div class="pgs-woo-api-field-group">
								<div class="pgs-woo-api-form-groups">
									<label><?php esc_html_e( 'Alert Title','pgs-woo-api' ); ?></label>
									<input id="pgs-woo-api-whatsapp-no-validation" type="text" name="pgs[delete_account_alert_title]" class="pgs-woo-api-form-control" value="<?php echo esc_attr( $delete_account_alert_title ); ?>" />
									<span id="validation_status"></span>
								</div>
							</div>
							<div class="pgs-woo-api-field-group">
								<div class="pgs-woo-api-form-groups">
									<label><?php esc_html_e( 'Alert Message','pgs-woo-api' ); ?></label>
									<input id="pgs-woo-api-whatsapp-no-validation" type="text" name="pgs[delete_account_alert_message]" class="pgs-woo-api-form-control" value="<?php echo esc_attr( $delete_account_alert_message ); ?>" />
									<span id="validation_status"></span>
								</div>
							</div>
						</div>
					</div>
				</div>
                <?php
            }
			?>
        </div>
    </form>
</div>
    <?php
}?>
