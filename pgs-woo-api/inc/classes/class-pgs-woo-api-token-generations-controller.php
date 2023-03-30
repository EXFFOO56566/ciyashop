<?php
/**
 * Token Generations Controller
 *
 * @package PGS Woo Api/classes
 */

if ( ! defined( 'ABSPATH' ) ) {
	exit; // Exit if accessed directly.
}

require_once( PGS_API_PATH . 'inc/classes/class-pgs-auth-header-generator.php' );

/**
 * PGS_WOO_API_Token_Generations_Controller class.
 */
class PGS_WOO_API_Token_Generations_Controller {

	private $auth_data;
	private $url;
	private $method;
	private $auth_credentials;
	private $step;
	private $oauth_verifier;

	/**
	 * Constructor.
	 */
	public function __construct( $auth_data, $url, $method, $step, $oauth_verifier = '' ) {
		$this->auth_data      = $auth_data;
		$this->url            = $url;
		$this->method         = $method;
		$this->step           = $step;
		$this->oauth_verifier = $oauth_verifier;

		if ( 1 === $this->step ) {
			$this->pgs_woo_api_set_step_1();
		} elseif ( 2 === $this->step ) {
			$this->pgs_woo_api_set_step_2();
		} else {
			$this->view( $this->step );
		}
	}

	/**
	 * View function.
	 *
	 * @param string|int $view  View type.
	 * @param array      $param Array of params.
	 * @return void
	 */
	public function view( $view, $param = array() ) {

		if ( 2 === $this->step ) {
			echo $this->get_step_2_form_data( $param );
		} elseif ( 3 === $this->step ) {
			echo $this->get_step_3_form_data( $param );
		} else {
			echo $this->get_step_1_form_data();
		}
	}

	/**
	 * The get_step_1_form_data function.
	 *
	 * @return string
	 */
	public function get_step_1_form_data() {

		$html  = '<div class="step-1">';
		$html .= '<label>' . esc_html__( 'Click on below button to start token generation process', 'pgs-woo-api' ) . '</label>';
		$html .= '<input type="hidden" name="step" value="1" />';
		$html .= '</div>';
		$html .= '<button id="stp-1" class="button button-primary token-gen-pro">' . esc_attr__( 'Let\'s go!', 'pgs-woo-api' ) . '</button>';

		return $html;
	}

	/**
	 * The get_step_2_form_data function.
	 *
	 * @param array $auth_data_arr Auth data.
	 * @return string
	 */
	public function get_step_2_form_data( $auth_data_arr ) {

		$html = '<div class="step-2">';
			if (
				( isset( $auth_data_arr['oauth_token'] ) && ! empty( $auth_data_arr['oauth_token'] ) ) &&
				( isset( $auth_data_arr['oauth_token_secret'] ) && ! empty( $auth_data_arr['oauth_token_secret'] ) )
			) {
				$input_type = ( defined( 'DEBUG_PGS_WOO_API' ) && DEBUG_PGS_WOO_API ) ? 'text' : 'hidden';

				$html .= '<label>' . esc_html__( 'Verification token', 'pgs-woo-api' ) . '</label>';
				$html .= '<div class="pgs-woo-api-form-groups">';
				$html .= '<input type="' . esc_attr( $input_type ) . '" name="oauth_consumer_key" value="' . $auth_data_arr['oauth_consumer_key'] . '" class="pgs-woo-api-form-control" />';
				$html .= '</div><div class="pgs-woo-api-form-groups">';
				$html .= '<input type="' . esc_attr( $input_type ) . '" name="oauth_consumer_secret" value="' . $auth_data_arr['oauth_consumer_secret'] . '" class="pgs-woo-api-form-control" />';
				$html .= '</div><div class="pgs-woo-api-form-groups">';
				$html .= '<input type="' . esc_attr( $input_type ) . '" name="oauth_token" value="' . $auth_data_arr['oauth_token'] . '" class="pgs-woo-api-form-control" />';
				$html .= '</div><div class="pgs-woo-api-form-groups">';
				$html .= '<input type="' . esc_attr( $input_type ) . '" name="oauth_token_secret" value="' . $auth_data_arr['oauth_token_secret'] . '" class="pgs-woo-api-form-control" />';
				$html .= '</div>';
				$html .= '<input type="' . esc_attr( $input_type ) . '" name="step" value="2" />';

				if ( isset( $auth_data_arr['recall'] ) && ! empty( $auth_data_arr['recall'] ) ) {
					$html .= '<p>' . esc_html__( 'Try to re submit. if still not get final token and token secret then refresh this page and do it again.', 'pgs-woo-api' ) . '</p>';
					$html .= '<input type="hidden" name="oauth_verifier" value="' . $auth_data_arr['oauth_verifier'] . '"/>';
				} else {
					$html .= '<p>';
					$html .= esc_html__( 'Click on below link to get authorization token.', 'pgs-woo-api' );
					$html .= '</p>';
					$html .= '<p><a href="' . $auth_data_arr['url'] . '" target="_blank"><strong>' . esc_html__( 'Get verification token', 'pgs-woo-api' ) . '</strong></a></p>';
					$html .= '<p>';
					$html .= esc_html__( 'Onces you click above link, it will redirect you to new tab which will prompt to authorize API, On clicking "Authorize" button, you will get verification token. Copy that token and paste it to below "Verification Token" input box.', 'pgs-woo-api' );
					$html .= '</p>';
					$html .= '<label>' . esc_html__( 'Verification Token', 'pgs-woo-api' ) . '</label>';
					$html .= '<input type="text" class="pgs-woo-api-form-control" name="oauth_verifier" value="" />';
				}
				$html .= '</div>';
				$html .= '<p class="submit">';
				$html .= '<button id="stp-2" class="button button-primary token-gen-pro">' . esc_attr__( 'Continue', 'pgs-woo-api' ) . '</button>';
				$html .= '</p>';
			} else {
				$html .= '<p>' . esc_html__( 'HTTP Header Authorization is disabled. Please check our document for troubleshooting.', 'pgs-woo-api' );
				$html .= ' <a href="http://docs.potenzaglobalsolutions.com/ciya-shop-mobile-apps/" target="_blank">' . esc_html__( 'Click heare', 'pgs-woo-api' ) . '</a></p>';
			}
		return $html;
	}

	/**
	 * The get_step_3_form_data function.
	 *
	 * @param array $param Array of params.
	 * @return string
	 */
	public function get_step_3_form_data( $param ) {
		$message = esc_html__( 'You\'re ready to call API.', 'pgs-woo-api' );
		$status  = 'success';

		echo pgs_woo_api_admin_notice_render( $message, $status );

		$oauth_token                          = ( isset( $param['oauth_token'] ) ) ? $param['oauth_token'] : '';
		$oauth_token_secret                   = ( isset( $param['oauth_token_secret'] ) ) ? $param['oauth_token_secret'] : '';
		$pgs_auth                             = get_option( 'app_auth' );
		$pgs_auth['pgs_auth']['token']        = sanitize_text_field( $oauth_token );
		$pgs_auth['pgs_auth']['token_secret'] = sanitize_text_field( $oauth_token_secret );

		update_option( 'app_auth', $pgs_auth );

		// Authorize.
		$html  = '<div class="pgs-woo-api-form-groups">';
		$html .= '<label>' . esc_html__( 'Token', 'pgs-woo-api' ) . '</label>';
		$html .= '<input type="text" name="pgs_auth[token]" class="pgs-woo-api-form-control" value="' . esc_attr( $oauth_token ) . '" readonly=""/>';
		$html .= '</div>';
		$html .= '<div class="pgs-woo-api-form-groups">';
		$html .= '<label>' . esc_html__( 'Token Secret', 'pgs-woo-api' ) . '</label>';
		$html .= '<input type="text" name="pgs_auth[token_secret]" class="pgs-woo-api-form-control" value="' . esc_attr( $oauth_token_secret ) . '" readonly=""/>';
		$html .= '</div>';

		return $html;
	}

	/**
	 * The pgs_woo_api_set_step_1 function.
	 *
	 * @return array
	 */
	public function pgs_woo_api_set_step_1() {

		$ch       = curl_init( $this->url );
		$response = '';

		curl_setopt( $ch, CURLOPT_TIMEOUT, 30 );
		curl_setopt( $ch, CURLOPT_RETURNTRANSFER, 1 );

		$response  = curl_exec( $ch );
		$error_msg = '';

		if ( curl_error( $ch ) ) {
			$error_msg = curl_error( $ch );
		}

		curl_close( $ch );

		$response    = json_decode( $response );
		$request_url = $response->authentication->oauth1->request;

		if ( isset( $request_url ) ) {
			$this->url = $request_url;
			$result    = $this->pgs_woo_api_curl();

			if ( 'success' === $result['status'] ) {
				$data = $result['data'];

				parse_str( $data, $output );

				$auth_data          = $this->auth_data;
				$oauth_consumer_key = $auth_data['oauth_consumer_key'];
				$oauth_token        = $output['oauth_token'];
				$oauth_token_secret = $output['oauth_token_secret'];

				$newurl = home_url( "oauth1/authorize?oauth_consumer_key=$oauth_consumer_key&oauth_token=$oauth_token&oauth_token_secret=$oauth_token_secret" );

				$auth_data_arr                       = $this->auth_data;
				$auth_data_arr['oauth_token']        = $oauth_token;
				$auth_data_arr['oauth_token_secret'] = $oauth_token_secret;
				$auth_data_arr['url']                = $newurl;
				$this->step                          = 2;

				$this->view( $this->step, $auth_data_arr );
			}
		} else {
			$result = array(
				'status' => 'error',
				'msg'    => esc_html__( 'Something went wrong.', 'pgs-woo-api' ),
			);
		}
		return $result;
	}

	/**
	 * The pgs_woo_api_set_step_2 function.
	 *
	 * @return void
	 */
	public function pgs_woo_api_set_step_2() {
		global $auth_data_arr;

		if ( '' !== $this->oauth_verifier ) {
			$result = $this->pgs_woo_api_curl();
			if ( 'success' === $result['status'] ) {
				$this->step = 3;
				$this->view( $this->step, $result['data'] );
			} else {
				$auth_data_arr                   = $this->auth_data;
				$auth_data_arr['oauth_verifier'] = $this->oauth_verifier;
				$newurl                          = $this->url;
				$auth_data_arr['recall']         = 'yes';
				$auth_data_arr['url']            = $newurl;
				$this->step                      = 2;

				$this->view( $this->step, $auth_data_arr );
			}
		} else {
			$message = esc_html__( 'Please enter verification token.', 'pgs-woo-api' );
			$status  = 'error';

			echo pgs_woo_api_admin_notice_render( $message, $status );

			$auth_data_arr        = $this->auth_data;
			$auth_data            = $this->auth_data;
			$oauth_consumer_key   = $auth_data['oauth_consumer_key'];
			$oauth_token          = $auth_data['oauth_token'];
			$oauth_token_secret   = $auth_data['oauth_token_secret'];
			$newurl               = home_url( "oauth1/authorize?oauth_consumer_key = $oauth_consumer_key&oauth_token = $oauth_token&oauth_token_secret = $oauth_token_secret" );
			$auth_data_arr['url'] = $newurl;
			$this->step           = 2;

			$this->view( $this->step, $auth_data_arr );
		}
	}

	/**
	 * The pgs_woo_api_curl function.
	 *
	 * @return array
	 */
	public function pgs_woo_api_curl() {

		$oauth       = new Pgs_auth_header_generator( $this->auth_data, $this->url, $this->method, $this->oauth_verifier );
		$header_data = $oauth->pgs_woo_api_header_data();

		$curl = curl_init();

		curl_setopt_array(
			$curl,
			array(
				CURLOPT_URL            => $this->url,
				CURLOPT_RETURNTRANSFER => true,
				CURLOPT_ENCODING       => '',
				CURLOPT_MAXREDIRS      => 10,
				CURLOPT_TIMEOUT        => 30,
				CURLOPT_HTTP_VERSION   => CURL_HTTP_VERSION_1_1,
				CURLOPT_CUSTOMREQUEST  => 'GET',
				CURLOPT_HTTPHEADER     => array(
					"authorization: $header_data",
					'cache-control: no-cache',
				),
			)
		);

		$response = curl_exec( $curl );
		$err      = curl_error( $curl );

		curl_close( $curl );

		if ( $err ) {
			$err    = 'cURL Error #: ' . $err;
			$result = array(
				'status' => 'error',
				'msg'    => $err,
			);
		} else {

			$result = array(
				'status' => 'success',
				'data'   => $response,
			);

			if ( isset( $this->oauth_verifier ) && ! empty( $this->oauth_verifier ) ) {

				parse_str( $response, $output );

				if ( isset( $output['oauth_token'] ) && ! empty( $output['oauth_token'] ) ) {
					$result = array(
						'status' => 'success',
						'data'   => $output,
					);
				} else {
					$result = array(
						'status' => 'error',
						'msg'    => $response,
					);
				}
			}
		}
		return $result;
	}
}
