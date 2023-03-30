<?php
if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}

class PGS_Woo_API_WooWallet {

	public function __construct() {
		add_action( 'wp_loaded', array( $this, 'cteate_woowallet_topup_page_if_not_exist' ) );
		add_shortcode( 'pgs-woo-api-woo-wallet-topup', array( $this, 'woowallet_topup_shortcode_callback' ) );
	}

	/**
	 * Create rechargeable product if not exist
	 */
	public static function cteate_woowallet_topup_page_if_not_exist(){
		if ( ! get_option( '_pgs_woo_api_woowallet_topup_page' ) ) {
			PGS_Woo_API_WooWallet::create_woowallet_topup_page();
		}
	}

	/**
	 * create rechargeable product
	 */
	private function create_woowallet_topup_page() {
		$page_args = array(
			'post_title'    => 'App Wallet Topup',
			'post_status'   => 'publish',
			'post_type'     => 'page',
			'post_content'  => '[pgs-woo-api-woo-wallet-topup]',
			'post_author'   => 1,
		);

		// Insert the page into the database.
		$page_id = wp_insert_post( $page_args );
		if ( ! is_wp_error( $page_id ) ) {
			update_option( '_pgs_woo_api_woowallet_topup_page', $page_id );
		}
	}

	/**
	 * Wallet shortcode callback
	 * @param array $atts
	 * @return string
	 */
	public static function woowallet_topup_shortcode_callback( $atts ) {
		ob_start();
		?>
		<div class="woo-wallet-topup-container">
			<?php $current_user = wp_get_current_user();?>
			<div class="woo-wallet-content-heading">
				<span class="woo-wallet-client-welcome"><?php printf( __( 'Welcome %s,', 'pgs-woo-api' ), '<strong>' . esc_html( $current_user->display_name ) .'</strong>' );?></span>
				<span class="woo-wallet-balance"><?php _e( 'Balance', 'pgs-woo-api' ); ?>: <span class="woo-wallet-balance-amt"><?php echo woo_wallet()->wallet->get_wallet_balance( get_current_user_id() ); ?></span></span>
			</div>
			<form method="post" action="">
				<div class="woo-wallet-add-amount">
					<?php
					$min_amount = woo_wallet()->settings_api->get_option('min_topup_amount', '_wallet_settings_general', 0);
					$max_amount = woo_wallet()->settings_api->get_option('max_topup_amount', '_wallet_settings_general', '');
					wp_nonce_field('woo_wallet_topup', 'woo_wallet_topup');
					$page_id = get_the_ID() ? get_the_ID() : 0;
					?>
					<input type="hidden" id="woo_add_to_wallet_source" name="woo_add_to_wallet_source" value="<?php echo esc_attr( $page_id );?>">
					<input type="hidden" id="woo_add_to_wallet_user_id" name="user_id" value="<?php echo esc_attr( get_current_user_id() );?>">
					<div class="pgs-woo-wallet-input-group">
						<input type="number" step="0.01" min="<?php echo $min_amount; ?>" max="<?php echo $max_amount; ?>" name="woo_wallet_balance_to_add" id="woo_wallet_balance_to_add" class="woo-wallet-balance-to-add input-text pgs-woo-wallet-form-control" placeholder="<?php _e( 'Enter amount', 'pgs-woo-api' ); ?>" required="" />
						<div class="pgs-woo-wallet-input-group-append">
							<input type="submit" name="woo_add_to_wallet" class="woo-add-to-wallet pgs-woo-wallet-input-btn" value="<?php _e( 'Add', 'pgs-woo-api' ); ?>" />
						</div>
					</div>
				</div>
			</form>
		</div>
		<?php
		return ob_get_clean();
	}
}

new PGS_Woo_API_WooWallet();

