<?php
// Do not allow directly accessing this file.
if ( ! defined( 'ABSPATH' ) ) {
	exit( 'Direct script access denied.' );
}
/**
 * Setting page for support settings
 */
add_action('admin_menu', 'pgs_woo_api_option_page_support');
function pgs_woo_api_option_page_support(){
	add_submenu_page( "pgs-woo-api-settings", esc_html('Support','pgs-woo-api'), esc_html('Support','pgs-woo-api'), 'manage_options', 'pgs-woo-api-support-settings', 'pgs_woo_api_support_token_callback' );
}


function pgs_woo_api_support_token_callback(){
	$plugin_name = "Ciya Shop Mobile App API";
	$auth_token = PGS_WOO_API_Support::pgs_woo_api_verify_plugin();
	$purchase_key_android = get_option('pgs_woo_api_plugin_android_purchase_key');
	$purchase_key_ios = get_option('pgs_woo_api_plugin_ios_purchase_key');
	$icon_android = 'dashicons dashicons-admin-network';
	$icon_ios = 'dashicons dashicons-admin-network';
	$pgs_token_android = get_option('pgs_woo_api_pgs_token_android');
	$input_type_android = 'text';
	if( $pgs_token_android && !empty($pgs_token_android)){
		$icon_android = 'dashicons dashicons-yes';
		$input_type_android = 'password';
		$purchase_key_ios = '';
	}
	$pgs_token_ios = get_option('pgs_woo_api_pgs_token_ios');
	$input_type_ios = 'text';
	if( $pgs_token_ios && !empty($pgs_token_ios)){
		$icon_ios = 'dashicons dashicons-yes';
		$input_type_ios = 'password';
		$purchase_key_android = '';
	}

	if( !empty($pgs_token_android) && !empty($pgs_token_ios)){
		$purchase_key_android = get_option('pgs_woo_api_plugin_android_purchase_key');
		$purchase_key_ios = get_option('pgs_woo_api_plugin_ios_purchase_key');
	}
	$panel = (isset($_GET['panel']) && !empty($_GET['panel']))?$_GET['panel']:'';
	$active_tab_1 = 'nav-tab-active'; $active_tab_2 = '';
	if($panel == "support-doc"){
		$panel = "support-doc";
		$active_tab_1 = '';
		$active_tab_2 = 'nav-tab-active';
	}
	?>
	<div class="wrap pgs-woo-api-api-admin-page">
		<?php
		require_once( PGS_API_PATH . 'inc/support/templates/support-header.php' );?>
		<div class="wrap-top gradient-bg">
			<h2 class="nav-tab-wrapper">
				<a class="nav-tab <?php echo esc_attr( $active_tab_1 ); ?>" href="<?php echo admin_url('admin.php?page=pgs-woo-api-support-settings')?>"><?php esc_html_e('License','pgs-woo-api')?></a>
				<?php if(!empty($auth_token)){?>
				<a class="nav-tab <?php echo esc_attr( $active_tab_2 ); ?>" href="<?php echo admin_url('admin.php?page=pgs-woo-api-support-settings&panel=support-doc')?>"><?php esc_html_e('Support','pgs-woo-api')?></a>
				<?php }?>
			</h2>
			<div class="support-bg">
				<?php
				if($panel != "support-doc"){
					if(isset($_POST['submit-token-android']) || isset($_POST['submit-token-ios']) ){
						?>
						<div class="cdhl-theme-active theme-panel"></div>
						<h3><?php esc_html_e( 'Please Enter Item Purchase Code', 'pgs-woo-api' ); ?></h3>
						<p class="pgs-woo-api-theme-description">
							<?php printf(esc_html__('Thank you for installing %s plugin! Please enter the item purchase code of %1$s plugin to be able to install demos and get updates of the plugin.', 'pgs-woo-api'), $plugin_name); ?>
						</p>
						<?php
						if(isset($_POST['submit-token-android']) || isset($_POST['submit-token-ios']) ){
							if( isset($_POST['submit-token-android'])){
								$prefix = "android";
								$error = get_site_transient("pgs_woo_api_auth_notice_$prefix");
							} elseif( isset($_POST['submit-token-ios'])){
								$prefix = "ios";
								$error = get_site_transient("pgs_woo_api_auth_notice_$prefix");
							}

							if( ! empty($_POST['pgs_woo_api_verify_plugin']["purchase_key_$prefix"]) && !empty($auth_token) && empty($error) ) {?>
								<div class="pgs-woo-api-admin-important-success-notice">
									<?php echo esc_html( get_site_transient("pgs_woo_api_pgs_auth_msg_$prefix") ); ?>
								</div>
								<?php
								delete_site_transient("pgs_woo_api_pgs_auth_msg_$prefix");
							}
							if( !empty($error) ) {
								if($prefix == "android"){
									$icon_android = 'dashicons dashicons-no';
								} elseif($prefix == "ios"){
									$icon_ios = 'dashicons dashicons-no';
								}
								?>
									<div class="pgs-woo-api-admin-important-notice">
										<?php
										echo esc_html($error);
										delete_site_transient("pgs_woo_api_auth_notice_$prefix");
										?>
									</div>
								<?php
							}
							if( empty($_POST['pgs_woo_api_verify_plugin']["purchase_key_$prefix"]) && empty($auth_token) ) {
								if($prefix == "android"){
									$icon_android = 'dashicons dashicons-admin-network';
								} elseif($prefix == "ios"){
									$icon_ios = 'dashicons dashicons-admin-network';
								}
								?>
								<div class="pgs-woo-api-admin-important-notice">
									<?php echo esc_html__('Please enter product purchase key to activate plugin.', 'pgs-woo-api');?>
								</div><?php
							}
						}
					}

					if(!empty($auth_token)){
						printf( wp_kses(
								__( '<p>Click here for Goto <a href="%1$s">API Settings</a>.', 'pgs-woo-api' ),
								array(
									'p' => array(),
									'a' => array(
										'href' => array()
									)
								)
							),
							esc_url('admin.php?page=pgs-woo-api-token-settings')
						);
					}
					if ( empty($auth_token) ) {
						$purchase_key_android = '';
						$purchase_key_ios = '';
					}
					?>
					<div class="pgs-woo-api-box-row">
						<div class="pgs-woo-api-box">

							<form id="pgs_woo_api_verify_item_android" class="pgs_woo_api_verify_plugin" method="post" action="">
								<label>CiyaShop Native Android Application based on WooCommerce</label>
								<div class="pgs-woo-api-token-fields">
									<input type="hidden" name="pgs_woo_api_nonce" value="<?php echo wp_create_nonce('pgs-woo-api-verify-token');?>" />
									<input type="hidden" name="pgs_woo_api_verify_plugin[item_key_android]" value="c7ec1dc95001d57cdedfe122569648dc" />
									<input type="<?php echo esc_attr($input_type_android)?>" name="pgs_woo_api_verify_plugin[purchase_key_android]" value="<?php echo !empty($purchase_key_android) ? esc_attr( $purchase_key_android ) : ''; ?>" placeholder="Enter purchase code" />
									<span class="<?php echo esc_attr($icon_android)?>"></span>
								</div>
								<p class="submit"><input name="submit-token-android" id="submit-token-android" class="button button-primary button-large" value="<?php esc_attr_e( 'Check', 'pgs-woo-api' )?>" type="submit">
							</form>
						</div>
						<div class="pgs-woo-api-box">
							<form id="pgs_woo_api_verify_item_ios" class="pgs_woo_api_verify_plugin" method="post" action="">
								<label>CiyaShop Native iOS Application based on WooCommerce</label>
								<div class="pgs-woo-api-token-fields">
									<input type="hidden" name="pgs_woo_api_nonce" value="<?php echo wp_create_nonce('pgs-woo-api-verify-token');?>" />
									<input type="hidden" name="pgs_woo_api_verify_plugin[item_key_ios]" value="7884626eb301b0f657bb23894fd2dbfe" />
									<input type="<?php echo esc_attr($input_type_ios)?>" name="pgs_woo_api_verify_plugin[purchase_key_ios]" value="<?php echo !empty($purchase_key_ios) ? esc_attr( $purchase_key_ios ) : ''; ?>" placeholder="Enter purchase code" />
									<span class="<?php echo $icon_ios; ?>"></span>
								</div>
								<p class="submit"><input name="submit-token-ios" id="submit-token-ios" class="button button-primary button-large" value="<?php esc_attr_e( 'Check', 'pgs-woo-api' )?>" type="submit">
							</form>
						</div>
					</div>

					<?php if( empty($auth_token) ) { ?>
						<h3><?php esc_html_e( 'Instructions For Find Purchase Code', 'pgs-woo-api' ); ?></h3>
						<ul>
							<li>
								<span> <?php esc_html_e('01', 'pgs-woo-api');?>  </span>
								<?php esc_html_e( 'Log into your Envato Market account.', 'pgs-woo-api' );?>
							</li>
							<li>
								<span> <?php esc_html_e('02', 'pgs-woo-api');?>  </span>
								<?php esc_html_e( 'Hover the mouse over your username at the top of the screen.', 'pgs-woo-api' )?>
							</li>
							<li>
								<span> <?php esc_html_e('03', 'pgs-woo-api');?>  </span>
								<?php esc_html_e( 'Click \'Downloads\' from the drop down menu.', 'pgs-woo-api' );?>
							</li>
							<li>
								<span> <?php esc_html_e('04', 'pgs-woo-api');?>  </span>
								<?php
								printf(
									wp_kses(
										__( 'Click \'License certificate & purchase code\' (available as PDF or text file). For more information <a href="%1$s" target="_blank">click here</a>.', 'pgs-woo-api' ),
										array(
											'br' => array(),
											'strong' => array(),
											'a' => array(
												'href' => array(),
												'target' => array()
												)
										)
									),
									esc_url('https://help.market.envato.com/hc/en-us/articles/202822600-Where-Is-My-Purchase-Code'), $plugin_name
								);?>
							</li>
						</ul>
						<?php
					}
				} elseif( $panel == "support-doc" ){
					require_once( PGS_API_PATH . 'inc/support/templates/support-docs.php' );
				}?>
			</div>
		</div>
	</div>
	<?php
}
