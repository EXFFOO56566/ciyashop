<?php
$input = file_get_contents("php://input");
$request = json_decode($input,true);

if(isset($request['os']) && $request['os'] == 'android'){
	
}
pgs_woo_api_remove_admin_bar();?>
<!DOCTYPE html>
<html <?php language_attributes(); ?>>
<head>
<meta charset="<?php echo esc_attr(get_bloginfo( 'charset' )); ?>">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="format-detection" content="telephone=no" />
<link rel="profile" href="http://gmpg.org/xfn/11">
<link rel="pingback" href="<?php echo esc_url(get_bloginfo( 'pingback_url' )); ?>">
<?php $app_color = pgs_woo_api_get_app_color();?>
<style>
.pgs-woo-api-web-view { background-color:#fff; }
.pgs-woo-api-web-view .woocommerce .woocommerce-checkout-review-order-table .order-total td{ color: <?php echo esc_attr($app_color['primary_color'])?> !important; }
.pgs-woo-api-web-view .woocommerce button, input[type="button"], input[type="submit"]{ background-color: <?php echo esc_attr($app_color['primary_color'])?> !important; }
.pgs-woo-api-web-view .woocommerce .input-text:focus { border-width: 2px; border-color: <?php echo esc_attr($app_color['primary_color'])?> !important; }
.pgs-woo-api-web-view .select2-container--default.select2-container--open li:hover{ background-color: <?php echo esc_attr($app_color['primary_color'])?> !important; }
.pgs-woo-api-web-view .select2-container--default.select2-container--open .select2-results__option--highlighted{ background-color: <?php echo esc_attr($app_color['primary_color'])?> !important; }
.pgs-woo-api-web-view .woocs_auto_switcher{ display: none;}
.wpml-ls-statics-footer{ display: none;}
<?php
$is_pgs_multisteps = is_pgs_multisteps_checkout_active();
if( $is_pgs_multisteps ){
    PGS_WOO_API_MultiSteps_Checkout::pgs_woo_api_wcmc_inline_checkout_style($app_color);
}?>
</style>
<?php wp_head(); ?>
</head>
<body <?php body_class('pgs-woo-api-web-view');?>>
<?php
$current_id = get_the_id();
$pgs_woo_api_home_option = get_option('pgs_woo_api_home_option');
$web_view_pages = $pgs_woo_api_home_option['web_view_pages'];
$web_view_pages_ids = array_column( $web_view_pages, 'web_view_pages_page_id' );
if( in_array( $current_id, $web_view_pages_ids ) ) {
	$web_view_pages_indx = array_search( $current_id, $web_view_pages_ids );
	echo $web_view_page_detail = $web_view_pages[$web_view_pages_indx]['web_view_page_title_page_id'];
}
?>
<?php the_content(); ?>
<?php wp_footer(); ?>
</body>
</html>