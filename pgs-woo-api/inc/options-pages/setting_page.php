<?php
/**
 * Setting page for auth and other options settings
 */

require_once( PGS_API_PATH . 'inc/classes/class-pgs-auth-header-generator.php' );

/**
 * Function pgs_woo_api_option_page_submenu.
 */
function pgs_woo_api_option_page_submenu() {
	add_submenu_page( 'pgs-woo-api-settings', esc_html( 'API Settings', 'pgs-woo-api' ), esc_html( 'API Settings', 'pgs-woo-api' ), 'manage_options', 'pgs-woo-api-token-settings', 'pgs_woo_api_token_callback' );
}
add_action( 'admin_menu', 'pgs_woo_api_option_page_submenu' );

/**
 * Function pgs_woo_api_token_callback.
 *
 * @return void
 */
function pgs_woo_api_token_callback() {

	require_once( PGS_API_PATH . 'inc/options-pages/option_functions.php' );
	$pgs_auth          = array();
	$pgs_woo_api       = get_option( 'app_auth', array() );
	$client_key        = ( isset( $pgs_woo_api['pgs_auth']['client_key'] ) && ! empty( $pgs_woo_api['pgs_auth']['client_key'] ) ) ? $pgs_woo_api['pgs_auth']['client_key'] : '';
	$client_secret     = ( isset( $pgs_woo_api['pgs_auth']['client_secret'] ) && ! empty( $pgs_woo_api['pgs_auth']['client_secret'] ) ) ? $pgs_woo_api['pgs_auth']['client_secret'] : '';
	$token             = ( isset( $pgs_woo_api['pgs_auth']['token'] ) && ! empty( $pgs_woo_api['pgs_auth']['token'] ) ) ? $pgs_woo_api['pgs_auth']['token'] : '';
	$token_secret      = ( isset( $pgs_woo_api['pgs_auth']['token_secret'] ) && ! empty( $pgs_woo_api['pgs_auth']['token_secret'] ) ) ? $pgs_woo_api['pgs_auth']['token_secret'] : '';
	$woo_client_key    = ( isset( $pgs_woo_api['woo_auth']['client_key'] ) && ! empty( $pgs_woo_api['woo_auth']['client_key'] ) ) ? $pgs_woo_api['woo_auth']['client_key'] : '';
	$woo_client_secret = ( isset( $pgs_woo_api['woo_auth']['client_secret'] ) && ! empty( $pgs_woo_api['woo_auth']['client_secret'] ) ) ? $pgs_woo_api['woo_auth']['client_secret'] : '';

	$google_map_api_key = '';
	$pgs_google_keys    = get_option( 'pgs_google_keys' );

	if ( isset( $pgs_google_keys['google_keys']['google_map_api_key'] ) && ! empty( $pgs_google_keys['google_keys']['google_map_api_key'] ) ) {
		$google_map_api_key = $pgs_google_keys['google_keys']['google_map_api_key'];
	}

	$coupon       = '';
	$coupon_msg   = '';
	$order        = '';
	$order_msg    = '';
	$pgs_not_code = get_option( 'pgs_not_code' );

	if ( isset( $pgs_not_code ) && ! empty( $pgs_not_code ) ) {
		$coupon     = ( isset( $pgs_not_code['1']['title'] ) ) ? $pgs_not_code['1']['title'] : '';
		$coupon_msg = ( isset( $pgs_not_code['1']['message'] ) ) ? $pgs_not_code['1']['message'] : '';
		$order      = ( isset( $pgs_not_code['2']['title'] ) ) ? $pgs_not_code['2']['title'] : '';
		$order_msg  = ( isset( $pgs_not_code['2']['message'] ) ) ? $pgs_not_code['2']['message'] : '';
	}

	$android_l_s_key = '';
	$android_key     = get_option( 'android_l_s_key' );
	if ( isset( $android_key ) && ! empty( $android_key ) ) {
		$android_l_s_key = $android_key;
	}

	$pushstatus  = get_option( 'pgs_push_status' );
	$push_status = ( isset( $pushstatus ) && ! empty( $pushstatus ) ) ? $pushstatus : 'enable';
	$pushmode    = get_option( 'pgs_push_mode' );
	$push_mode   = ( isset( $pushmode ) && ! empty( $pushmode ) ) ? $pushmode : 'live';

	$pgsiosappurl    = get_option( 'pgs_ios_app_url' );
	$pgs_ios_app_url = ( isset( $pgsiosappurl ) ) ? $pgsiosappurl : '';

	$activevendor  = get_option( 'pgs_active_vendor' );
	$active_vendor = ( isset( $activevendor ) && ! empty( $activevendor ) ) ? $activevendor : 'dokan';

	$pemfiledevpass    = get_option( 'pem_file_dev_pass' );
	$pem_file_dev_pass = ( isset( $pemfiledevpass ) && ! empty( $pemfiledevpass ) ) ? $pemfiledevpass : '';

	$pemfilepropass    = get_option( 'pem_file_pro_pass' );
	$pem_file_pro_pass = ( isset( $pemfilepropass ) && ! empty( $pemfilepropass ) ) ? $pemfilepropass : '';

	// Check vendor is active.
	$is_vendor  = pgs_woo_api_is_vendor_plugin_active();
	$auth_token = PGS_WOO_API_Support::pgs_woo_api_verify_plugin();

	$contact_mail_options_data = pgs_woo_api_get_contact_mail_options_data();
	$contact_us_recipient      = $contact_mail_options_data['contact_us_recipient'];
	$contact_us_from_name      = $contact_mail_options_data['contact_us_from_name'];
	$contact_us_from_email     = $contact_mail_options_data['contact_us_from_email'];

	$forgot_password_mail_options_data = pgs_woo_api_get_forgot_password_mail_options_data();
	$forgot_password_subject           = $forgot_password_mail_options_data['forgot_password_subject'];
	$forgot_password_from_name         = $forgot_password_mail_options_data['forgot_password_from_name'];
	$forgot_password_from_email        = $forgot_password_mail_options_data['forgot_password_from_email'];

	$vendor_contact_mail_options_data = pgs_woo_api_get_vendor_contact_mail_options_data();
	$vendor_contact_subject           = $vendor_contact_mail_options_data['vendor_contact_subject'];
	$vendor_contact_from_name         = $vendor_contact_mail_options_data['vendor_contact_from_name'];
	$vendor_contact_from_email        = $vendor_contact_mail_options_data['vendor_contact_from_email'];
	?>
	<div class="wrap">
		<h2></h2><?php echo do_action( 'pgs_woo_api_promo_notice' ); ?>
		<div class="wrap-top gradient-bg">
			<h2 class="wp-heading-inline"><?php esc_html_e( 'API Settings', 'pgs-woo-api' ); ?></h2>
			<div class="pgs-woo-api-right">
				<div class="pgs-woo-api-right-heading"><?php esc_html_e( 'Publish', 'pgs-woo-api' ); ?></div>
				<?php if ( ! empty( $auth_token ) ) { ?>
				<div class="publish-btn-box">
					<span class="spinner"></span>
					<button id="publish-btn" type="submit" name="submit-api-auth" form="pgs-woo-api-setting-form" class="pgs-woo-api-btn button button-primary Submit-btn" value="Update"><?php esc_html_e( 'Update', 'pgs-woo-api' ); ?></button>
				</div>
				<?php } ?>
			</div>
		</div>
		<form method="POST" action="" name="pgs-woo-api-setting-form" id="pgs-woo-api-setting-form" enctype="multipart/form-data">
			<div id="pgs-woo-api-tabs">
				<ul>
					<li><a href="#pgs-woo-api-tabs-api-keys"><?php esc_html_e( 'API Keys', 'pgs-woo-api' ); ?></a></li>
					<?php
					if ( ! empty( $auth_token ) ) {
						?>
						<li><a href="#pgs-woo-api-tabs-wooCommerce-api"><?php esc_html_e( 'WooCommerce API', 'pgs-woo-api' ); ?></a></li>
						<li><a href="#pgs-woo-api-tabs-goole-map-key"><?php esc_html_e( 'Google Maps API', 'pgs-woo-api' ); ?></a></li>
						<li><a href="#pgs-woo-api-tabs-push-notification"><?php esc_html_e( 'Push Notification', 'pgs-woo-api' ); ?></a></li>
						<li><a href="#pgs-woo-api-tabs-app-url"><?php esc_html_e( 'App URL', 'pgs-woo-api' ); ?></a></li>
						<?php
						/**
						 * If both vendor plugin active then this menu will appear
						 */
						if ( $is_vendor['vendor_count'] > 1 ) {
							?>
							<li><a href="#pgs-woo-api-tabs-vendor-settings"><?php esc_html_e( 'Vendor Settings', 'pgs-woo-api' ); ?></a></li>
							<?php
						}
						/**
						 * Test api
						 */
						?>
						<li><a href="#pgs-woo-api-tabs-test-api"><?php esc_html_e( 'Test API', 'pgs-woo-api' ); ?></a></li>
						<li><a href="#pgs-woo-api-tabs-emails"><?php esc_html_e( 'Emails', 'pgs-woo-api' ); ?></a></li>
						<li><a href="#pgs-woo-api-tabs-credentials-code"><?php esc_html_e( 'Credentials Code', 'pgs-woo-api' ); ?></a></li>
						<?php
					}
					?>
				</ul>
				<div id="pgs-woo-api-tabs-api-keys">
					<div class="pgs-woo-api-panel" >
						<div class="pgs-woo-api-panel-body">
							<div class="pgs-woo-api-panel-heading">
								<?php esc_html_e( 'OAuth Credentials', 'pgs-woo-api' ); ?>
							</div>
							<?php
							if ( empty( $auth_token ) ) {
								?>
								<p class="PGS-WOO-API-Support-info">
								<?php
								echo sprintf(
									wp_kses(
										/* translators: %s link */
										__( '<strong>Alert: </strong>In order to configure OAuth Credentials data, Please verify your item purchase by providing purchase key <a href="%s">here</a>.', 'pgs-woo-api' ),
										array(
											'a'      => array(
												'href' => array(),
											),
											'p'      => array(
												'class' => array(),
											),
											'strong' => array(
												'style' => array(),
											),
										)
									),
									esc_url( admin_url( 'admin.php?page=pgs-woo-api-support-settings' ) )
								);
								?>
								</p>
								<?php
							} else {
								?>
								<div class="description mb-20"><?php esc_html_e( 'These credentials are just for the backup purpose.', 'pgs-woo-api' ); ?></div>
								<div class="description mb-20">
									<div class="htaccess-note"><?php esc_html_e( 'Note: Before performing token generation process, please check your htaccess file add below code to the top of the .htaccess file for header authorizations', 'pgs-woo-api' ); ?>.
									<a id="htaccess-code-toggle" href="javascript:void(0);"><?php esc_html_e( 'click here for htaccess file code', 'pgs-woo-api' ); ?></a></div>
									<textarea id="htaccess-code" style="width: 100%;height:50px;resize: none; display: none;">RewriteEngine on&#13;&#10;SetEnvIf Authorization .+ HTTP_AUTHORIZATION=$0</textarea></p>
								</div>
								<div class="pgs-woo-api-field-groups">
									<div class="pgs-woo-api-form-groups">
										<label><?php esc_html_e( 'Client Key', 'pgs-woo-api' ); ?></label>
										<input type="text" name="pgs_auth[client_key]" class="pgs-woo-api-form-control client_key" value="<?php echo esc_attr( $client_key ); ?>" />
										<!--button class="" id=""><span class="dashicons dashicons-edit"></span></button-->
									</div>
									<div class="pgs-woo-api-form-groups">
										<label><?php esc_html_e( 'Client Secret', 'pgs-woo-api' ); ?></label>
										<input type="text" name="pgs_auth[client_secret]" class="pgs-woo-api-form-control client_secret" value="<?php echo esc_attr( $client_secret ); ?>" />
									</div>

									<?php
									if ( '' !== $token ) {
										?>
										<div class="pgs-woo-api-form-groups">
											<label><?php esc_html_e( 'Token', 'pgs-woo-api' ); ?></label>
											<input type="text" name="pgs_auth[token]" class="pgs-woo-api-form-control token" value="<?php echo esc_attr( $token ); ?>" readonly=""/>
										</div>
										<div class="pgs-woo-api-form-groups">
											<label><?php esc_html_e( 'Token Secret', 'pgs-woo-api' ); ?></label>
											<input type="text" name="pgs_auth[token_secret]" class="pgs-woo-api-form-control token_secret" value="<?php echo esc_attr( $token_secret ); ?>" readonly=""/>
										</div>
										<?php
									} else {
										echo token_generations_pro();
									}
									?>
								</div>
								<?php
							}
							?>
						</div>
					</div>
				</div><!-- #pgs-woo-api-tabs-api-keys -->
				<?php
				if ( ! empty( $auth_token ) ) {
					?>
					<div id="pgs-woo-api-tabs-wooCommerce-api">

						<div class="pgs-woo-api-panel" >
							<div class="pgs-woo-api-panel-body">
							<div class="pgs-woo-api-panel-heading">
								<?php esc_html_e( 'WooCommerce Credentials', 'pgs-woo-api' ); ?>
							</div>
							<div class="description mb-20"><?php esc_html_e( 'These credentials are just for the backup purpose.', 'pgs-woo-api' ); ?></div>
								<div class="pgs-woo-api-field-groups">
									<div class="pgs-woo-api-form-groups">
										<label><?php esc_html_e( 'Consumer Key', 'pgs-woo-api' ); ?></label>
										<input type="text" name="woo_auth[client_key]" class="pgs-woo-api-form-control consumer_key" value="<?php echo esc_attr( $woo_client_key ); ?>" />
									</div>
									<div class="pgs-woo-api-form-groups">
										<label><?php esc_html_e( 'Consumer Secret', 'pgs-woo-api' ); ?></label>
										<input type="text" name="woo_auth[client_secret]" class="pgs-woo-api-form-control consumer_secret" value="<?php echo esc_attr( $woo_client_secret ); ?>" />
									</div>
								</div>
							</div>
						</div>
					</div><!-- #pgs-woo-api-tabs-wooCommerce-api -->

					<div id="pgs-woo-api-tabs-push-notification">

						<div class="pgs-woo-api-panel" >
							<div class="pgs-woo-api-panel-body">
								<div class="pgs-woo-api-panel-heading"><?php esc_html_e( 'Push Notification', 'pgs-woo-api' ); ?></div>
								<div class="pgs-woo-api-field-groups">
									<div class="pgs-woo-api-form-groups radio-button-inline">
										<label><?php esc_html_e( 'Status', 'pgs-woo-api' ); ?></label><br />
										<label><input type="radio" name="push_status" class="pgs-woo-api-form-control" value="enable" <?php echo ( 'enable' === $push_status ) ? 'checked=""' : ''; ?> /><?php esc_html_e( 'Enable', 'pgs-woo-api' ); ?></label>
										<label><input type="radio" name="push_status" class="pgs-woo-api-form-control" value="disable" <?php echo ( 'disable' === $push_status ) ? 'checked=""' : ''; ?> /><?php esc_html_e( 'Disable', 'pgs-woo-api' ); ?></label>
										<p class="description"><?php esc_html_e( 'Enable / Disable Push notification', 'pgs-woo-api' ); ?></p>
									</div>
									<hr />
									<div class="pgs-woo-api-form-groups radio-button-inline">
										<label><?php esc_html_e( 'Mode', 'pgs-woo-api' ); ?></label><br />
										<label><input type="radio" name="push_mode" class="pgs-woo-api-form-control" value="sandbox" <?php echo ( 'sandbox' === $push_mode ) ? 'checked=""' : ''; ?> /><?php esc_html_e( 'Sandbox', 'pgs-woo-api' ); ?></label>
										<label><input type="radio" name="push_mode" class="pgs-woo-api-form-control" value="live" <?php echo ( 'live' === $push_mode ) ? 'checked=""' : ''; ?> /><?php esc_html_e( 'Live', 'pgs-woo-api' ); ?></label>
										<p class="description"><?php esc_html_e( 'IOS Push notification mode', 'pgs-woo-api' ); ?></p>
									</div>
									<hr />
									<div class="pgs-woo-api-form-groups ">
										<label><?php esc_html_e( 'Upload Development Pem File', 'pgs-woo-api' ); ?></label><br />
										<input type="file" name="pem_file_dev" id="pem_file_dev" style="display:none" />
										<?php
										$img          = wp_mime_type_icon( 'video/document' );
										$pem_file_dev = get_option( 'pem_file_dev' );

										if ( isset( $pem_file_dev ) && ! empty( $pem_file_dev ) ) {
											?>
											<img src="<?php echo esc_url( $img ); ?>" alt="No image found" />
											<p class="description" id="pem-dev-file-desc"><?php echo $pem_file_dev; ?></p>
											<?php
										}
										?>
										<button class="button button-primary" id="open_pem_file_dev"><?php esc_html_e( 'File Upload', 'pgs-woo-api' ); ?></button>
										<p class="description"><?php esc_html_e( 'Upload IOS Push notification Pem File for development', 'pgs-woo-api' ); ?></p>
									</div>
									<hr />
									<div class="pgs-woo-api-form-groups">
										<label><?php esc_html_e( 'Password for Development Pem File', 'pgs-woo-api' ); ?></label>
										<input type="password" name="pem_file_dev_pass" class="pgs-woo-api-form-control" value="<?php echo esc_attr( $pem_file_dev_pass ); ?>" />
									</div>
									<hr />
									<div class="pgs-woo-api-form-groups">
										<label><?php esc_html_e( 'Upload Distribution Pem File', 'pgs-woo-api' ); ?></label><br />
										<input type="file" name="pem_file_pro" id="pem_file_pro" style="display:none" />
										<?php
										$pem_file = get_option( 'pem_file_pro' );
										if ( isset( $pem_file ) && ! empty( $pem_file ) ) {
											?>
											<img src="<?php echo esc_url( $img ); ?>" alt="<?php esc_html_e( 'No image found', 'pgs-woo-api' ); ?>" />
											<p class="description" id="pem-pro-file-desc"><?php echo $pem_file; ?></p>
											<?php
										}
										?>
										<button class="button button-primary" id="open_pem_file_pro"><?php esc_html_e( 'File Upload', 'pgs-woo-api' ); ?></button>
										<p class="description"><?php esc_html_e( 'Upload IOS Push notification Pem File for production', 'pgs-woo-api' ); ?></p>
									</div>
									<hr />
									<div class="pgs-woo-api-form-groups">
										<label><?php esc_html_e( 'Password for Distribution Pem File', 'pgs-woo-api' ); ?></label>
										<input type="password" name="pem_file_pro_pass" class="pgs-woo-api-form-control" value="<?php echo esc_attr( $pem_file_pro_pass ); ?>" />
									</div>
									<hr />
									<div class="pgs-woo-api-field-groups">
										<div class="pgs-woo-api-form-groups">
											<label><?php esc_html_e( 'Android Legacy server key', 'pgs-woo-api' ); ?></label>
											<input type="text" name="android_l_s_key" class="pgs-woo-api-form-control" value="<?php echo esc_attr( $android_l_s_key ); ?>" />
										</div>
									</div>
									<hr />
									<div class="pgs-woo-api-field-groups">
										<div class="pgs-woo-api-form-groups">
											<label><?php esc_html_e( 'Coupon code notification title', 'pgs-woo-api' ); ?></label>
											<input type="text" name="pgs_not_code[1][title]" class="pgs-woo-api-form-control" value="<?php echo esc_attr( $coupon ); ?>" />
										</div>
										<div class="pgs-woo-api-form-groups">
											<label><?php esc_html_e( 'Coupon code notification message', 'pgs-woo-api' ); ?></label>
											<input type="text" name="pgs_not_code[1][message]" class="pgs-woo-api-form-control" value="<?php echo esc_attr( $coupon_msg ); ?>" />
											<p class="description"><?php esc_html_e( '{{coupon}} add this code for show in notification', 'pgs-woo-api' ); ?></p>
										</div>

										<div class="pgs-woo-api-form-groups">
											<label><?php esc_html_e( 'Order status notification title', 'pgs-woo-api' ); ?></label>
											<input type="text" name="pgs_not_code[2][title]" class="pgs-woo-api-form-control" value="<?php echo esc_attr( $order ); ?>" />
										</div>
										<div class="pgs-woo-api-form-groups">
											<label><?php esc_html_e( 'Order status notification message', 'pgs-woo-api' ); ?></label>
											<input type="text" name="pgs_not_code[2][message]" class="pgs-woo-api-form-control" value="<?php echo esc_attr( $order_msg ); ?>" />
											<p class="description"><?php esc_html_e( '{{status}}, {{order_id}} add this code for show in notification', 'pgs-woo-api' ); ?></p>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div><!-- #pgs-woo-api-tabs-push-notification -->

					<div id="pgs-woo-api-tabs-app-url">
						<div class="pgs-woo-api-panel" >
							<div class="pgs-woo-api-panel-body">
								<div class="pgs-woo-api-panel-heading">
									<?php esc_html_e( 'App URL', 'pgs-woo-api' ); ?>
								</div>
								<div class="pgs-woo-api-field-groups">
									<div class="pgs-woo-api-form-groups">
										<label><?php esc_html_e( 'IOS App URL', 'pgs-woo-api' ); ?></label>
										<input type="text" name="pgs_ios_app_url" class="pgs-woo-api-form-control" value="<?php echo esc_attr( $pgs_ios_app_url ); ?>" />
									</div>
								</div>
							</div>
						</div>
					</div><!-- #pgs-woo-api-tabs-app-url -->
					<?php
					if ( $is_vendor['vendor_count'] > 1 ) {
						?>
						<div id="pgs-woo-api-tabs-vendor-settings">

							<div class="pgs-woo-api-panel" >
								<div class="pgs-woo-api-panel-body">
								<div class="pgs-woo-api-panel-heading"><?php esc_html_e( 'Vendor Settings', 'pgs-woo-api' ); ?></div>


									<div class="pgs-woo-api-field-groups">
										<div class="pgs-woo-api-form-groups">
											<label><?php esc_html_e( 'Status', 'pgs-woo-api' ); ?></label><br />
											<label><input type="radio" name="active_vendor" class="pgs-woo-api-form-control" value="dokan" <?php echo ( 'dokan' === $active_vendor ) ? 'checked=""' : ''; ?> /><?php esc_html_e( 'Dokan', 'pgs-woo-api' ); ?></label>
											<label><input type="radio" name="active_vendor" class="pgs-woo-api-form-control" value="wc_marketplace" <?php echo ( 'wc_marketplace' === $active_vendor ) ? 'checked=""' : ''; ?> /><?php esc_html_e( 'WC Marketplace', 'pgs-woo-api' ); ?></label>
											<p class="description"><?php esc_html_e( 'Please select vendor plugin', 'pgs-woo-api' ); ?></p>
										</div>
									</div>
								</div>
							</div>
						</div><!-- #pgs-woo-api-tabs-vendor-settings -->
						<?php
					}
					?>
					<div id="pgs-woo-api-tabs-test-api">

						<div class="pgs-woo-api-panel" >
							<div class="pgs-woo-api-panel-body">
							<div class="pgs-woo-api-panel-heading"><?php esc_html_e( 'Test API', 'pgs-woo-api' ); ?></div>
							<p class="description mb-20"><?php esc_html_e( 'Click for check test API.', 'pgs-woo-api' ); ?></p>


								<div class="pgs-woo-api-field-groups">
									<div class="pgs-woo-api-form-groups">
										<?php // [pgs_woo_api_check_oauth_connection] ?>

										<input type="button" name="submit" id="test-api-btn" class="button button-primary" value="<?php esc_attr_e( 'Check', 'pgs-woo-api' ); ?>">
										<div class="pgs-loader"></div>
									</div>
								</div>
							</div>
						</div>
					</div><!-- #pgs-woo-api-tabs-test-api -->

					<div id="pgs-woo-api-tabs-emails">
						<div class="pgs-woo-api-panel" >
							<div class="pgs-woo-api-panel-body">
								<div class="pgs-woo-api-panel-heading">
									<?php esc_html_e( 'Emails', 'pgs-woo-api' ); ?>
								</div>

								<h4><?php esc_html_e( 'Contact Us', 'pgs-woo-api' ); ?></h4>
								<div class="pgs-woo-api-field-groups">
									<div class="pgs-woo-api-form-groups">
										<label><?php esc_html_e( 'Recipient(s)', 'pgs-woo-api' ); ?></label>
										<input type="text" name="pgs_woo_api_emails_contact_recipient" class="pgs-woo-api-form-control" value="<?php echo esc_attr( $contact_us_recipient ); ?>" />
									</div>
									<div class="pgs-woo-api-form-groups">
										<label ><?php esc_html_e( 'From Name', 'pgs-woo-api' ); ?></label>
										<input type="text" name="pgs_woo_api_emails_contact_from_name" class="pgs-woo-api-form-control" value="<?php echo esc_attr( $contact_us_from_name ); ?>" />
									</div>
									<div class="pgs-woo-api-form-groups">
										<label ><?php esc_html_e( 'From Address', 'pgs-woo-api' ); ?></label>
										<input type="text" name="pgs_woo_api_emails_contact_from_address" class="pgs-woo-api-form-control" value="<?php echo esc_attr( $contact_us_from_email ); ?>" />
									</div>
								</div>

								<h4><?php esc_html_e( 'Forgot Password', 'pgs-woo-api' ); ?></h4>
								<div class="pgs-woo-api-field-groups">
									<div class="pgs-woo-api-form-groups">

										<label><?php esc_html_e( 'Subject', 'pgs-woo-api' ); ?></label>
										<input type="text" name="pgs_woo_api_emails_forgot_password_subject" class="pgs-woo-api-form-control" value="<?php echo esc_attr( $forgot_password_subject ); ?>" />
									</div>
									<div class="pgs-woo-api-form-groups">
										<label ><?php esc_html_e( 'From Name', 'pgs-woo-api' ); ?></label>
										<input type="text" name="pgs_woo_api_emails_forgot_password_from_name" class="pgs-woo-api-form-control" value="<?php echo esc_attr( $forgot_password_from_name ); ?>" />
									</div>
									<div class="pgs-woo-api-form-groups">
										<label ><?php esc_html_e( 'From Address', 'pgs-woo-api' ); ?></label>
										<input type="text" name="pgs_woo_api_emails_forgot_password_address" class="pgs-woo-api-form-control" value="<?php echo esc_attr( $forgot_password_from_email ); ?>" />
									</div>
								</div>

								<h4><?php esc_html_e( 'Vendor Contact Email', 'pgs-woo-api' ); ?></h4>
								<div class="pgs-woo-api-field-groups">
									<div class="pgs-woo-api-form-groups">
										<label><?php esc_html_e( 'Subject', 'pgs-woo-api' ); ?></label>
										<input type="text" name="pgs_woo_api_emails_vendor_contact_subject" class="pgs-woo-api-form-control" value="<?php echo esc_attr( $vendor_contact_subject ); ?>" />
									</div>
									<div class="pgs-woo-api-form-groups">
										<label ><?php esc_html_e( 'From Name', 'pgs-woo-api' ); ?></label>
										<input type="text" name="pgs_woo_api_emails_vendor_contact_from_name" class="pgs-woo-api-form-control" value="<?php echo esc_attr( $vendor_contact_from_name ); ?>" />
									</div>
									<div class="pgs-woo-api-form-groups">
										<label ><?php esc_html_e( 'From Address', 'pgs-woo-api' ); ?></label>
										<input type="text" name="pgs_woo_api_emails_vendor_contact_address" class="pgs-woo-api-form-control" value="<?php echo esc_attr( $vendor_contact_from_email ); ?>" />
									</div>
								</div>
							</div>
						</div>
					</div><!-- #pgs-woo-api-tabs-emails -->

					<div id="pgs-woo-api-tabs-goole-map-key">
						<div class="pgs-woo-api-panel" >
							<div class="pgs-woo-api-panel-body">
								<div class="pgs-woo-api-panel-heading">
									<?php esc_html_e( 'Google Map API', 'pgs-woo-api' ); ?>
								</div>
								<div class="description mb-20">
									<?php
									echo sprintf(
										wp_kses(
											/* translators: %s link */
											__( 'You can get a Google Maps API from <a href="%1$s" target="_blank">here.</a>', 'pgs-woo-api' ),
											array(
												'a' => array(
													'href' => array(),
													'target' => array(),
												),
											)
										),
										esc_url( 'https://developers.google.com/maps/documentation/javascript/' )
									);
									?>
								</div>
								<p class="description note-desc"><strong><?php esc_html_e( 'Note', 'pgs-woo-api' ); ?>: </strong><?php esc_html_e( 'This key used for the geofencing functionality, for initializing google map using google map API.', 'pgs-woo-api' ); ?></p>
								<div class="pgs-woo-api-field-groups">
									<div class="pgs-woo-api-form-groups">
										<label><?php esc_html_e( 'Google Maps API Key', 'pgs-woo-api' ); ?></label>
										<input type="text" name="google_keys[google_map_api_key]" class="pgs-woo-api-form-control" value="<?php echo esc_attr( $google_map_api_key ); ?>" />
									</div>
								</div>
							</div>
						</div>
					</div><!-- #pgs-woo-api-tabs-goole-map-key -->
					<div id="pgs-woo-api-tabs-credentials-code">
						<div class="pgs-woo-api-panel" >
							<div class="pgs-woo-api-panel-body">
								<div class="pgs-woo-api-panel-heading">
									<?php esc_html_e( 'Credentials Code', 'pgs-woo-api' ); ?>
								</div>
								<p class="description mb-20"><?php esc_html_e( 'Click on respected icons for generate the code of OAuth key and url for Android and iOS application.', 'pgs-woo-api' ); ?></p>
								<div class="pgs-woo-api-field-groups">
									<div class="pgs-woo-api-form-groups">
										<?php
										$credentials_array = array(
											esc_html__( 'Client Key', 'pgs-woo-api' )      => $client_key,
											esc_html__( 'Client Secret', 'pgs-woo-api' )   => $client_secret,
											esc_html__( 'Token', 'pgs-woo-api' )           => $token,
											esc_html__( 'Token Secret', 'pgs-woo-api' )    => $token_secret,

											esc_html__( 'Consumer Key', 'pgs-woo-api' )    => $woo_client_key,
											esc_html__( 'Consumer Secret', 'pgs-woo-api' ) => $woo_client_secret,
										);

										$class       = 'device-select';
										$missing_key = array();

										foreach ( $credentials_array as $key => $value ) {
											if ( '' === $value ) {
												$missing_key[] = $key;
												?>
												<p>
													<?php
													printf(
														/* translators: %s parameter key */
														esc_html__( '"%s" is missing.', 'pgs-woo-api' ),
														$key
													);
													?>
												</p>
												<?php
											}
										}
										if ( ! empty( $missing_key ) ) {
											$class .= ' pgs-hidden';
										}
										?>
										<div class="<?php echo esc_attr( $class ); ?>">

											<a href="javascript:void(0)" class="button button-primary android-device credentials-code-device-img" data-target="credentials-code-android">
												<img class="img-responsive" src="<?php echo esc_url( trailingslashit( PGS_API_URL ) . 'img/android/android.png' ); ?>" />
											</a>
											<a href="javascript:void(0)" class="button button-primary ios-device credentials-code-device-img" data-target="credentials-code-ios">
												<img class="img-responsive" src="<?php echo esc_url( trailingslashit( PGS_API_URL ) . 'img/ios/ios.png' ); ?>" />
											</a>

										</div>
									</div>
									<div class="pgs-woo-api-form-groups">
										<textarea class="pgs-woo-api-credentials-code pgs-hidden" readonly="readonly"></textarea>
									</div>
									<div class="pgs-woo-api-form-groups">
										<input name="text" class="pgs-site-url pgs-hidden" value="<?php echo esc_attr( get_site_url() ); ?>" />
										<input name="submit" id="credentials-code-api-btn" class="button button-primary" style="display: none;" value="Copy" type="button">
									</div>
								</div>
							</div>
						</div>
					</div><!-- #pgs-woo-api-tabs-credentials-code -->
					<?php
				}
				?>
			</div>
		</form>
	</div>
	<?php
}
