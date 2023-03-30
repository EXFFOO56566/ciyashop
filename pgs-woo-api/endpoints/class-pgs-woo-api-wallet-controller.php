<?php
/**
 * Wallet controller.
 *
 * @package PGS Woo Api/endpoints
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}

/**
 * PGS_WOO_API_Wallet_Controller class.
 */
class PGS_WOO_API_Wallet_Controller extends PGS_WOO_API_Controller {

	/**
	 * Endpoint namespace.
	 *
	 * @var string
	 */
	protected $namespace = 'pgs-woo-api/v1';

	/**
	 * Route base.
	 *
	 * @var string
	 */
	protected $rest_base = 'wallet';

	/**
	 * Wallet provider.
	 *
	 * @var string
	 */
	protected $provider = 'woo-wallet';

	/**
	 * Constructor.
	 */
	public function __construct() {
		$this->register_routes();
	}

	/**
	 * Register routes function.
	 *
	 * @return void
	 */
	public function register_routes() {

		add_action( 'rest_api_init', array( $this, 'pgs_woo_api_register_route' ) );
	}

	/**
	 * Register routes.
	 *
	 * @return void
	 */
	public function pgs_woo_api_register_route() {

		register_rest_route(
			$this->namespace,
			$this->rest_base,
			array(
				'methods'             => WP_REST_Server::CREATABLE, // 'POST'.
				'callback'            => array( $this, 'get_wallet' ),
				'permission_callback' => array( $this, 'pgs_woo_api_permission_callback' ),
			)
		);

		register_rest_route(
			$this->namespace,
			'wallet_referrals',
			array(
				'methods'             => WP_REST_Server::CREATABLE, // 'POST'.
				'callback'            => array( $this, 'wallet_referrals' ),
				'permission_callback' => array( $this, 'pgs_woo_api_permission_callback' ),
			)
		);

		register_rest_route(
			$this->namespace,
			'wallet_search_user',
			array(
				'methods'             => WP_REST_Server::CREATABLE, // 'POST'.
				'callback'            => array( $this, 'wallet_search_user' ),
				'permission_callback' => array( $this, 'pgs_woo_api_permission_callback' ),
			)
		);

		register_rest_route(
			$this->namespace,
			'wallet_transfer',
			array(
				'methods'             => WP_REST_Server::CREATABLE, // 'POST'.
				'callback'            => array( $this, 'wallet_transfer' ),
				'permission_callback' => array( $this, 'pgs_woo_api_permission_callback' ),
			)
		);
	}

	/**
	 * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/wallet
	 *
	 * @param WP_REST_Request $request Request data.
	 *      @type int    user_id                  User ID.
	 *
	 * @return WP_Error|WP_REST_Response
	 */
	public function get_wallet( $request ) {
		$params = $request->get_params();

		$default_data = array(
			'user_id'           => $params['user_id'],
			'provider'          => $this->provider,
			'balance'           => '0.00',
			'currency'          => get_woocommerce_currency_symbol(),
			'topup_page'        => '',
			'thankyou'          => '',
			'thankyou_endpoint' => '',
			'transactions'      => array(),
			'status'            => 'error',
		);

		// Retrieve user info by user ID.
		$user = get_userdata( absint( $params['user_id'] ) );

		// Bail early if user not found.
		if ( false === $user ) {
			return new WP_REST_Response( $default_data, 200 );
		}

		if ( 'woo-wallet' === $this->provider ) {
			$data = $this->get_wallet_woowallet( $params );
		}

		$data = wp_parse_args( $data, $default_data );

		return new WP_REST_Response( $data, 200 );
	}

	/**
	 * Get woo wallet data.
	 *
	 * @param array $params Array of params.
	 * @return array
	 */
	public function get_wallet_woowallet( $params ) {

		$data         = array();
		$transactions = array();

		$data['balance']  = woo_wallet()->wallet->get_wallet_balance( absint( $params['user_id'] ), 'edit' );
		$data['currency'] = get_woocommerce_currency_symbol();

		// Set Topup Page
		$topup_page                = pgs_woo_api_get_woowallet_topup_page();
		$data['topup_page']        = apply_filters( 'wpml_permalink', get_permalink( $topup_page ), ICL_LANGUAGE_CODE );
		$data['thankyou']          = wc_get_endpoint_url( 'order-received', '', wc_get_page_permalink( 'checkout' ) );
		$data['thankyou_endpoint'] = get_option( 'woocommerce_checkout_order_received_endpoint', 'order-received' );

		$wallet_transactions = get_wallet_transactions(
			array(
				'user_id' => absint( $params['user_id'] ),
			)
		);

		foreach ( $wallet_transactions as $wallet_transaction ) {

			$wallet_transaction_details = $wallet_transaction->details;

			$translated_strings = $this->get_translated_strings( $wallet_transaction->details );
			if ( is_array( $translated_strings ) && ! empty( $translated_strings ) && isset( $translated_strings[ ICL_LANGUAGE_CODE ] ) ) {
				$translated_strings_current_lang = $translated_strings[ ICL_LANGUAGE_CODE ];
				if ( isset( $translated_strings_current_lang->language ) && isset( $translated_strings_current_lang->value ) ) {
					$wallet_transaction_details = $translated_strings_current_lang->value;
				}
			}

			$transactions[] = array(
				'transaction_id' => $wallet_transaction->transaction_id,
				'user_id'        => $wallet_transaction->user_id,
				'type'           => $wallet_transaction->type,
				'amount'         => $wallet_transaction->amount,
				'currency'       => get_woocommerce_currency_symbol( $wallet_transaction->currency ),
				'details'        => $wallet_transaction_details,
				'date'           => $wallet_transaction->date,
			);
		}

		$data['transactions'] = $transactions;
		$data['status']       = 'success';

		return $data;
	}

	/**
	 * The get_translated_strings function.
	 *
	 * @param string $string String to translate.
	 * @return array
	 */
	public function get_translated_strings( $string = '' ) {
		global $wpdb;

		$static_string = '';

		if ( empty( $string ) ) {
			return array();
		}

		if ( strpos( $string, 'Wallet credit through purchase #' ) !== false ) {
			$string = 'Wallet credit through purchase #';
		}

		if ( strpos( $string, 'Wallet funds transfer to' ) !== false ) {
			// $string = 'Wallet funds transfer to %s';
		}

		$results = $wpdb->get_results(
			$wpdb->prepare(
				"SELECT translations.language, translations.value
				FROM {$wpdb->prefix}icl_string_translations translations
				JOIN {$wpdb->prefix}icl_strings strings
					ON strings.id = translations.string_id
				WHERE translations.status=%d
					AND strings.value = %s",
				ICL_TM_COMPLETE,
				$string
			),
			OBJECT_K
		);

		return $results;
	}

	/**
	 * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/wallet_referrals
	 *
	 * @param WP_REST_Request $request Request data.
	 *      @type int    user_id                  User ID.
	 *
	 * @return WP_Error|WP_REST_Response
	 */
	public function wallet_referrals( $request ) {
		$params = $request->get_params();

		$default_data = array(
			'user_id'       => absint( $params['user_id'] ),
			'provider'      => $this->provider,
			'balance'       => '0.00',
			'currency'      => get_woocommerce_currency_symbol(),
			'referral_link' => '',
			'referral_data' => array(),
			'status'        => 'error',
		);

		// Retrieve user info by user ID.
		$user = get_userdata( absint( $params['user_id'] ) );

		// Bail early if user not found.
		if ( false === $user ) {
			return new WP_REST_Response( $default_data, 200 );
		}

		if ( 'woo-wallet' === $this->provider ) {
			$data = $this->get_wallet_woowallet_referrals( $params );
		}

		$data = wp_parse_args( $data, $default_data );

		return new WP_REST_Response( $data, 200 );
	}

	/**
	 * The get_wallet_woowallet_referrals function.
	 *
	 * @param array $params Array of params.
	 * @return array
	 */
	public function get_wallet_woowallet_referrals( $params ) {
		$data         = array();
		$transactions = array();

		$wallet_actions            = WOO_Wallet_Actions::instance();
		$wallet_referrals          = $wallet_actions->actions['referrals'];
		$wallet_referrals_settings = $wallet_referrals->settings;

		if ( 'yes' !== $wallet_referrals_settings['enabled'] ) {
			$data['message'] = esc_html__( 'Referrals system not available.', 'pgs-woo-api' );
			return $data;
		}

		$data['balance']  = woo_wallet()->wallet->get_wallet_balance( absint( $params['user_id'] ), 'edit' );
		$data['currency'] = get_woocommerce_currency_symbol();

		$user_id                = absint( $params['user_id'] );
		$user                   = new WP_User( $user_id );
		$referral_url_by_userid = ( 'id' === $wallet_referrals_settings['referal_link'] ) ? true : false;
		$referral_url           = add_query_arg( $wallet_referrals->referral_handel, $user->user_login, site_url( '/' ) );

		if ( $referral_url_by_userid ) {
			$referral_url = add_query_arg( $wallet_referrals->referral_handel, $user->ID, site_url( '/' ) );
		}

		// Set Referral Link.
		$topup_page            = pgs_woo_api_get_woowallet_topup_page();
		$data['referral_link'] = $referral_url;
		$referring_visitor     = get_user_meta( $user_id, '_woo_wallet_referring_visitor', true ) ? get_user_meta( $user_id, '_woo_wallet_referring_visitor', true ) : 0;
		$referring_signup      = get_user_meta( $user_id, '_woo_wallet_referring_signup', true ) ? get_user_meta( $user_id, '_woo_wallet_referring_signup', true ) : 0;
		$referring_earning     = get_user_meta( $user_id, '_woo_wallet_referring_earning', true ) ? get_user_meta( $user_id, '_woo_wallet_referring_earning', true ) : 0;

		$data['referral_data'] = array(
			'referring_visitor' => $referring_visitor,
			'referring_signup'  => $referring_signup,
			'referring_earning' => $referring_earning,
		);

		$data['status'] = 'success';

		return $data;
	}

	/**
	 * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/wallet_search_user
	 *
	 * @param WP_REST_Request $request Request data.
	 *      @type int    user_id            User ID.
	 *      @type string term               Search term.
	 *
	 * @return WP_Error|WP_REST_Response
	 */
	public function wallet_search_user( $request ) {
		$params = $request->get_params();

		$default_data = array(
			'status'        => 'error',
			'message'       => '',
			'search_result' => array(),
		);

		// Retrieve user info by user ID.
		$current_user = get_userdata( absint( $params['user_id'] ) );

		// Bail early if user not found.
		if ( false === $current_user ) {
			$default_data['message'] = esc_html__( 'Invalid user.', 'pgs-woo-api' );
			return new WP_REST_Response( $default_data, 200 );
		}

		if ( 'woo-wallet' === $this->provider ) {
			/*
			 * Reference: woo-wallet/includes/class-woo-wallet-ajax.php : woo_wallet_user_search()
			 */
			if ( apply_filters( 'woo_wallet_user_search_exact_match', true ) ) {

				$search_email = ( isset( $params['term'] ) && ! empty( $params['term'] ) ) ? sanitize_email( $params['term'] ) : '';

				if ( ! isset( $params['term'] ) || empty( $params['term'] ) ) {
					$default_data['message'] = esc_html__( 'Please provide an email address.', 'pgs-woo-api' );
					return new WP_REST_Response( $default_data, 200 );
				}

				if ( empty( $search_email ) ) {
					$default_data['message'] = esc_html__( 'Please provide a valid email address.', 'pgs-woo-api' );
					return new WP_REST_Response( $default_data, 200 );
				}

				$search_user = get_user_by( apply_filters( 'woo_wallet_user_search_by', 'email' ), sanitize_text_field( wp_unslash( $params['term'] ) ) );
				if ( $search_user && $current_user->user_email !== $search_user->user_email ) {
					$data['status']          = 'success';
					$data['message']         = esc_html__( '1 user found.', 'pgs-woo-api' );
					$data['search_result'][] = array(
						/* translators: 1: display_name, 2: user_email */
						'label' => sprintf( _x( '%1$s (%2$s)', 'user autocomplete result', 'pgs-woo-api' ), $search_user->display_name, $search_user->user_email ),
						'value' => $search_user->ID,
					);
				} else {
					$data['message'] = esc_html__( 'No user found.', 'pgs-woo-api' );
				}
			} else {
				$blog_id = get_current_blog_id();
				if ( isset( $params['site_id'] ) ) {
					$blog_id = absint( $params['site_id'] );
				}

				$users = get_users(
					array(
						'blog_id'        => $blog_id,
						'search'         => '*' . sanitize_text_field( wp_unslash( $params['term'] ) ) . '*',
						'exclude'        => array( absint( $params['user_id'] ) ),
						'search_columns' => array( 'user_login', 'user_nicename', 'user_email' ),
					)
				);

				// $data['status'] = 'success';
				if ( ! empty( $users ) ) {
					foreach ( $users as $user ) {
						$data['status'] = 'success';

						/* translators: %s user count */
						$data['message'] = sprintf( _n( '%s user found.', '%s users found.', count( $users ), 'pgs-woo-api' ), number_format_i18n( count( $users ) ) );

						$data['search_result'][] = array(
							/* translators: 1: user_login, 2: user_email */
							'label' => sprintf( _x( '%1$s (%2$s)', 'user autocomplete result', 'pgs-woo-api' ), $user->user_login, $user->user_email ),
							'value' => $user->ID,
						);
					}
				} else {
					$data['message'] = esc_html__( 'No user found.', 'pgs-woo-api' );
				}
			}
		}

		$data = wp_parse_args( $data, $default_data );

		return new WP_REST_Response( $data, 200 );
	}

	/**
	 * URL : http://yourdomain.com/wp-json/pgs-woo-api/v1/wallet_transfer
	 *
	 * @param WP_REST_Request $request Request data.
	 *      @type int    user_id            User ID.
	 *      @type string term               Search term.
	 *
	 * @return WP_Error|WP_REST_Response
	 */
	public function wallet_transfer( $request ) {
		$params = $request->get_params();

		$default_data = array(
			'status'  => 'error',
			'message' => '',
		);

		// Retrieve user info by user ID.
		$current_user = get_userdata( absint( $params['user_id'] ) );

		// Bail early if user not found.
		if ( false === $current_user ) {
			$default_data['message'] = esc_html__( 'Invalid user.', 'pgs-woo-api' );
			return new WP_REST_Response( $default_data, 200 );
		}

		if ( 'woo-wallet' === $this->provider ) {
			if ( apply_filters( 'woo_wallet_is_enable_transfer', 'on' === woo_wallet()->settings_api->get_option( 'is_enable_wallet_transfer', '_wallet_settings_general', 'on' ) ) ) {

				$whom   = isset( $params['transfer_user_id'] ) && ! empty( $params['transfer_user_id'] ) ? absint( $params['transfer_user_id'] ) : '';
				$amount = isset( $params['transfer_amount'] ) && ! empty( $params['transfer_amount'] ) ? sanitize_text_field( wp_unslash( $params['transfer_amount'] ) ) : '';

				/* translators: %s User email */
				$credit_note = isset( $params['transfer_note'] ) && ! empty( $params['transfer_note'] ) ? sanitize_text_field( wp_unslash( $params['transfer_note'] ) ) : sprintf( __( 'Wallet funds received from %s', 'pgs-woo-api' ), $current_user->user_email );

				$whom = apply_filters( 'woo_wallet_transfer_user_id', $whom );

				if ( empty( $whom ) ) {
					$default_data['message'] = esc_html__( 'Please provide a transfer account.', 'pgs-woo-api' );
					return new WP_REST_Response( $default_data, 200 );
				}

				$whom = get_userdata( $whom );

				if ( false === $whom ) {
					$default_data['message'] = esc_html__( 'Please provide a valid transfer account.', 'pgs-woo-api' );
					return new WP_REST_Response( $default_data, 200 );
				}

				if ( empty( $amount ) ) {
					$default_data['message'] = esc_html__( 'Please provide a transfer amount.', 'pgs-woo-api' );
					return new WP_REST_Response( $default_data, 200 );
				}

				$amount = floatval( $amount );

				if ( ! $amount ) {
					$default_data['message'] = esc_html__( 'Please provide a valid transfer amount.', 'pgs-woo-api' );
					return new WP_REST_Response( $default_data, 200 );
				}

				$credit_note = apply_filters( 'woo_wallet_transfer_credit_transaction_note', $credit_note, $whom, $amount );

				/* translators: %s User email */
				$debit_note             = sprintf( __( 'Wallet funds transfer to %s', 'pgs-woo-api' ), $whom->user_email );
				$debit_note             = apply_filters( 'woo_wallet_transfer_debit_transaction_note', $debit_note, $whom, $amount );
				$transfer_charge_type   = woo_wallet()->settings_api->get_option( 'transfer_charge_type', '_wallet_settings_general', 'percent' );
				$transfer_charge_amount = woo_wallet()->settings_api->get_option( 'transfer_charge_amount', '_wallet_settings_general', 0 );
				$transfer_charge        = 0;

				if ( 'percent' === $transfer_charge_type ) {
					$transfer_charge = ( $amount * $transfer_charge_amount ) / 100;
				} else {
					$transfer_charge = $transfer_charge_amount;
				}
				$transfer_charge = apply_filters( 'woo_wallet_transfer_charge_amount', $transfer_charge, $whom );
				$credit_amount   = apply_filters( 'woo_wallet_transfer_credit_amount', $amount, $whom );
				$debit_amount    = apply_filters( 'woo_wallet_transfer_debit_amount', $amount + $transfer_charge, $whom );

				if ( woo_wallet()->settings_api->get_option( 'min_transfer_amount', '_wallet_settings_general', 0 ) ) {
					if ( woo_wallet()->settings_api->get_option( 'min_transfer_amount', '_wallet_settings_general', 0 ) > $amount ) {
						/* translators: %s Amount. */
						$default_data['message'] = sprintf( __( 'Minimum transfer amount is %s', 'pgs-woo-api' ), wc_price( woo_wallet()->settings_api->get_option( 'min_transfer_amount', '_wallet_settings_general', 0 ) ) );
						return new WP_REST_Response( $default_data, 200 );
					}
				}

				if ( floatval( $debit_amount ) > woo_wallet()->wallet->get_wallet_balance( absint( $params['user_id'] ), 'edit' ) ) {
					$default_data['message'] = __( 'Entered amount is greater than current wallet balance.', 'pgs-woo-api' );
					return new WP_REST_Response( $default_data, 200 );
				}

				$credit_transaction_id = woo_wallet()->wallet->credit( $whom->ID, $credit_amount, $credit_note );
				if ( $credit_transaction_id ) {
					do_action( 'woo_wallet_transfer_amount_credited', $credit_transaction_id, $whom->ID, absint( $params['user_id'] ) );
					$debit_transaction_id = woo_wallet()->wallet->debit( absint( $params['user_id'] ), $debit_amount, $debit_note );
					do_action( 'woo_wallet_transfer_amount_debited', $debit_transaction_id, absint( $params['user_id'] ), $whom->ID );
					update_wallet_transaction_meta( $debit_transaction_id, '_wallet_transfer_charge', $transfer_charge, absint( $params['user_id'] ) );

					$default_data['status']  = 'success';
					$default_data['message'] = __( 'Amount transferred successfully!', 'pgs-woo-api' );
				} else {
					$default_data['message'] = __( 'Unable to transfer amount.', 'pgs-woo-api' );
					return new WP_REST_Response( $default_data, 200 );
				}

				// return new WP_REST_Response( array('debug'), 200 );
			} else {
				$default_data['message'] = esc_html__( 'Wallet transfer not available.', 'pgs-woo-api' );
				return new WP_REST_Response( $default_data, 200 );
			}
		}

		$data = wp_parse_args( $data, $default_data );

		return new WP_REST_Response( $data, 200 );
	}
}

new PGS_WOO_API_Wallet_Controller;
