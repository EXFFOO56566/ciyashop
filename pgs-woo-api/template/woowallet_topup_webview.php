<?php
$input   = file_get_contents( 'php://input' );
$request = json_decode( $input, true );

$current_user    = null;
$current_user_id = 0;
$app_user_id     = 0;

if ( is_user_logged_in() ) {
	$current_user    = wp_get_current_user();
	$current_user_id = ( isset( $current_user->ID ) ? (int) $current_user->ID : 0 );
}

if ( $request ) {

	if ( isset( $request['user_id'] ) && ! empty( $request['user_id'] ) ) {
		$app_user_id = (int) sanitize_text_field( wp_unslash( $request['user_id'] ) );
	}

	$new_app_user = false;

	if ( $app_user_id && $current_user_id && $app_user_id !== $current_user_id ) {
		$new_app_user = get_user_by( 'id', $app_user_id );
	} elseif ( $app_user_id && ! $current_user_id ) {
		$new_app_user = get_user_by( 'id', $app_user_id );
	}

	if ( $new_app_user ) {
		wp_set_current_user( $new_app_user->ID, $new_app_user->user_login );
		wp_set_auth_cookie( $new_app_user->ID );
		do_action( 'wp_login', $new_app_user->user_login, $new_app_user );
		wp_safe_redirect( esc_url( add_query_arg( array() ) ) );
		exit();

		$current_user    = $new_app_user;
		$current_user_id = $new_app_user->ID;
	}
}

if ( ! is_user_logged_in() ) {
	wp_die(
		'<h1>' . __( 'Cheatin&#8217; uh?', 'pgs-woo-api' ) . '</h1>' .
		'<p>' . __( 'You are not allowed to access this page.', 'pgs-woo-api' ) . '</p>',
		403
	);
}
?>

<?php pgs_woo_api_remove_admin_bar();?>

<!DOCTYPE html>
<html class="pgs-woo-api-webview--html pgs-woo-api-webview-wallet_topup--html" <?php language_attributes(); ?>>
<head>
<meta charset="<?php echo esc_attr(get_bloginfo( 'charset' )); ?>">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="format-detection" content="telephone=no" />
<link rel="profile" href="http://gmpg.org/xfn/11">
<link rel="pingback" href="<?php echo esc_url(get_bloginfo( 'pingback_url' )); ?>">
<?php
$app_color              = pgs_woo_api_get_app_color();
$background_color       = $app_color['primary_color'];
$border_color           = $app_color['primary_color'];
$hover_background_color = '#0069d9';
$hover_border_color     = '#0062cc';
?>
<style>
.pgs-woo-api-webview-wallet_topup--html {
	margin-top: 0px !important;
}
.pgs-woo-api-webview-wallet_topup--html body {
	background: #fff;
}
.pgs-woo-api-webview-wallet_topup-wrapper .site-content {
	width: 100%;
	margin: 1.714285714rem 0;
}
.woo-wallet-topup-container {
	width: 100%;
	padding-right: 15px;
	padding-left: 15px;
	margin-right: auto;
	margin-left: auto;
}
@media (min-width: 576px) { .woo-wallet-topup-container { max-width: 540px; } }
@media (min-width: 768px) { .woo-wallet-topup-container { max-width: 720px; } }
@media (min-width: 992px) { .woo-wallet-topup-container { max-width: 960px; } }
@media (min-width: 1200px) { .woo-wallet-topup-container { max-width: 1140px; } }

.woo-wallet-topup-container .woo-wallet-content-heading {
	margin-bottom: 15px;
}

.woo-wallet-topup-container .woo-wallet-client-welcome,
.woo-wallet-topup-container .woo-wallet-balance {
	display: block;
	margin-bottom: 20px;
	font-size: 16px;
	line-height: 24px;
	font-family: arial;
	color: #6d6d6d;
}
.woo-wallet-topup-container .woo-wallet-balance{
	font-size: 14px;
}

.woo-wallet-topup-container .woo-wallet-balance .woo-wallet-balance-amt {
	font-size: normal;
}

.pgs-woo-wallet-input-group {
  position: relative;
  display: -ms-flexbox;
  display: flex;
  -ms-flex-wrap: wrap;
  flex-wrap: wrap;
  -ms-flex-align: stretch;
  align-items: stretch;
  width: 100%;
}

.pgs-woo-wallet-form-control {
  display: block;
  width: 100%;
  height: calc(1.5em + 0.75rem + 2px);
  padding: 0.375rem 0.75rem;
  font-size: 1rem;
  font-weight: 400;
  line-height: 1.5;
  color: #495057;
  background-color: #fff;
  background-clip: padding-box;
  border: 1px solid #ced4da;
  border-radius: 0.25rem;
  transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
}

.pgs-woo-wallet-input-group > .pgs-woo-wallet-form-control {
  position: relative;
  -ms-flex: 1 1 0%;
  flex: 1 1 0%;
  min-width: 0;
  margin-bottom: 0;
}

.pgs-woo-wallet-input-group > .pgs-woo-wallet-form-control:not(:last-child) {
  border-top-right-radius: 0;
  border-bottom-right-radius: 0;
}

.pgs-woo-wallet-input-group-append {
  margin-left: -1px;
}

.pgs-woo-wallet-input-group-append {
  display: -ms-flexbox;
  display: flex;
}

.pgs-woo-api-webview-wallet_topup--html .pgs-woo-wallet-input-btn {
  display: inline-block;
  font-weight: 400;
  color: #FFFFFF;
  text-align: center;
  vertical-align: middle;
  cursor: pointer;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
  background-color: <?php echo esc_attr( $background_color ); ?>;
  border: 1px solid <?php echo esc_attr( $border_color ); ?>;
  padding: 0.375rem 0.75rem !important;
  font-size: 1rem;
  line-height: 1.5;
  border-radius: 0.25rem;
  transition: color 0.15s ease-in-out, background-color 0.15s ease-in-out, border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
}

.pgs-woo-api-webview-wallet_topup--html .pgs-woo-wallet-input-btn:hover {
  color: #fff;
  text-decoration: none;
  background-color: <?php echo esc_attr( $hover_background_color ); ?>;
  border-color: <?php echo esc_attr( $hover_border_color ); ?>;
}

.pgs-woo-wallet-input-group-append .pgs-woo-wallet-input-btn {
  position: relative;
  z-index: 2;
  margin: 0;
  line-height: 100%;
}

.pgs-woo-wallet-input-group > .pgs-woo-wallet-input-group-append > .pgs-woo-wallet-input-btn {
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
}

.rtl .pgs-woo-wallet-input-group > .pgs-woo-wallet-form-control:not(:last-child) {
  border-top-left-radius: 0;
  border-bottom-left-radius: 0;
  border-top-right-radius: 0.25rem;
  border-bottom-right-radius: 0.25rem;
}
.rtl .pgs-woo-wallet-input-group > .pgs-woo-wallet-input-group-append > .pgs-woo-wallet-input-btn {
	border-top-right-radius: 0;
	border-bottom-right-radius: 0;
	border-top-left-radius: 0.25rem;
	border-bottom-left-radius: 0.25rem;
}







/*
.woo-wallet-balance-to-add.input-text {
	display: block;
	height: calc(1.5em + 0.75rem + 2px);
	padding: 0.375rem 0.75rem;
	font-size: 1rem;
	font-weight: 400;
	line-height: 1.5;
	color: #495057;
	background-color: #fff;
	background-clip: padding-box;
	border: 1px solid #ced4da;
	border-radius: 0.25rem;
	-webkit-transition: border-color 0.15s ease-in-out, -webkit-box-shadow 0.15s ease-in-out;
	transition: border-color 0.15s ease-in-out, -webkit-box-shadow 0.15s ease-in-out;
	-o-transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
	transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
	transition: border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out, -webkit-box-shadow 0.15s ease-in-out;
	width: calc( 100% - 82px );
	float: left;
	font-family: arial;
}
.woo-wallet-topup-container .woo-add-to-wallet {
  display: inline-block;
  font-weight: 400;
  color: #fff;
  text-align: center;
  vertical-align: middle;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
  background-color: <?php echo esc_attr( $background_color ); ?>;
  border: 1px solid <?php echo esc_attr( $border_color ); ?>;
  padding: 0.375rem 0.75rem;
  font-size: 1rem;
  line-height: 1.5;
  border-radius: 0.25rem;
  -webkit-transition: color 0.15s ease-in-out, background-color 0.15s ease-in-out, border-color 0.15s ease-in-out, -webkit-box-shadow 0.15s ease-in-out;
  transition: color 0.15s ease-in-out, background-color 0.15s ease-in-out, border-color 0.15s ease-in-out, -webkit-box-shadow 0.15s ease-in-out;
  -o-transition: color 0.15s ease-in-out, background-color 0.15s ease-in-out, border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
  transition: color 0.15s ease-in-out, background-color 0.15s ease-in-out, border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out;
  transition: color 0.15s ease-in-out, background-color 0.15s ease-in-out, border-color 0.15s ease-in-out, box-shadow 0.15s ease-in-out, -webkit-box-shadow 0.15s ease-in-out;
  width: 75px;
  float: right;
  margin-top: 0px;
  font-family: arial;
  text-transform: uppercase;
}
.woo-wallet-topup-container .woo-add-to-wallet:hover {
  color: #fff;
  text-decoration: none;
  background-color: <?php echo esc_attr( $hover_background_color ); ?>;
  border-color: <?php echo esc_attr( $hover_border_color ); ?>;
}
.woo-wallet-topup-container .woo-add-to-wallet:focus {
  outline: 0;
  -webkit-box-shadow: 0 0 0 0.2rem rgba(38, 143, 255, 0.5);
  box-shadow: 0 0 0 0.2rem rgba(38, 143, 255, 0.5);
}
*/

body.rtl .woo-wallet-balance-to-add.input-text {
	float: right;
}
body.rtl .woo-wallet-topup-container .woo-add-to-wallet {
	float: left;
}


@media screen and (max-width: 319px) {
	.woo-wallet-topup-container .woo-wallet-balance-to-add {
		width: calc( 100% - calc( .375rem - 4px ) );
	}
	.woo-wallet-topup-container .woo-add-to-wallet {
		width: 100%;
		margin-top: 10px;
	}
}
</style>
<?php wp_head(); ?>
</head>
<body <?php body_class('pgs-woo-api-webview pgs-woo-api-webview-wallet_topup');?>>
<div class="pgs-woo-api-webview-wrapper pgs-woo-api-webview-wallet_topup-wrapper">
	<?php
	/**
	 * Hook: pgs_woo_api_woowallet_topup_webview_before_main_content.
	 */
	do_action( 'pgs_woo_api_woowallet_topup_webview_before_main_content' );
	?>
	<div id="page" class="hfeed site">
		<div id="content" class="site-content">
			<?php
			/**
			 * Hook: pgs_woo_api_woowallet_topup_webview_content_wrapper_start.
			 */
			do_action( 'pgs_woo_api_woowallet_topup_webview_content_wrapper_start' );


				/**
				 * Hook: pgs_woo_api_woowallet_topup_webview_before_content_loop.
				 */
				do_action( 'pgs_woo_api_woowallet_topup_webview_before_content_loop' );

				if ( have_posts() ) :
					while ( have_posts() ) :
						the_post();

						the_content();
					endwhile; // End of the loop.
				endif;

				/**
				 * Hook: pgs_woo_api_woowallet_topup_webview_after_content_loop.
				 */
				do_action( 'pgs_woo_api_woowallet_topup_webview_after_content_loop' );

			/**
			 * Hook: pgs_woo_api_woowallet_topup_webview_content_wrapper_end.
			 * @hooked pgs_woo_api_woowallet_topup_webview_content_wrapper_container_end - 10
			 */
			do_action( 'pgs_woo_api_woowallet_topup_webview_content_wrapper_end' );
			?>
		</div><!-- #content .site-content -->
	</div><!-- #page .site -->
	<?php
	/**
	 * Hook: pgs_woo_api_woowallet_topup_webview_after_main_content.
	 */
	do_action( 'pgs_woo_api_woowallet_topup_webview_after_main_content' );

	wp_footer(); ?>
</div>
</body>
</html>
