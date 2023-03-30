/**
* App Custom Option Js
* @param-key:
*/
(function($){
	jQuery(document).ready(function() {

		/**
		* On update option or settings show loader
		*/
		if(document.getElementById('pgs-expand-div-options-products-carousel')){

			jQuery( ".pgs-woo-api-sort-products-carousel" ).sortable({
				//placeholder: "ui-state-highlight"
			});
			jQuery( ".pgs-woo-api-sort-products-carousel" ).disableSelection();

			jQuery('.pgs-expand-div-btn').each(function(){
				jQuery(this).on('click',function(){
					//alert('hi');
					jQuery(this).next('.pgs-expand-div-content').toggle('1000');
					jQuery("i", this).toggleClass("dashicons dashicons-arrow-up dashicons dashicons-arrow-down");
				});
			});
			var disable_url = jQuery('.disable-data-url').attr('data-url');
			jQuery(document).on('click','.carousel-box-status',function(){
				var carouselstatus = jQuery(this).val();
				if(carouselstatus == "enable"){
					var lblid = jQuery(this).attr('data-id');
					jQuery('#'+lblid).removeClass('pgs-woo-api-disable-lbl');
					jQuery('.is-disable-'+lblid).html('');
				} else {
					var lblid = jQuery(this).attr('data-id');

					jQuery('#'+lblid).addClass('pgs-woo-api-disable-lbl');
					jQuery('.is-disable-'+lblid).html('<img src="'+disable_url+'" alt="disable" />');
				}
			});
		}
		/** End **/

		/**
		* On update option or settings show loader
		*/
		if(document.getElementById('publish-btn')){
			jQuery('#publish-btn').on('click',function(){
				jQuery('.spinner').css('visibility','visible');
			});
		}
		/** End **/

		/**
		* Show / Hide options
		*/
		jQuery('.feature-box-status').on('click',function(){
			var status = jQuery(this).val();
			if(status == "disable"){
				jQuery('.feature-box').hide();
			} else {
				jQuery('.feature-box').show();
			}
		});
		/** End **/

		/**
		* Show / Hide options
		*/

		jQuery('.home-layout-button-status').on('click',function(){
			var status = jQuery(this).val();
			if(status == "default"){
				jQuery('.home-layout-scroll-options').show();
			} else {
				jQuery('.home-layout-scroll-options').show();
			}
		});

		/** End **/

		/**
		* Show / Hide options
		*/
		/*jQuery('.whatsapp-floating-button-status').on('click',function(){
			var status = jQuery(this).val();
			if(status == "disable"){
				jQuery('.pgs-woo-api-whatsapp-no').hide();
			} else {
				jQuery('.pgs-woo-api-whatsapp-no').show();
			}
		});*/
		/** End **/


		/**
		* Main Category Icon image.
		*/
		var get_cat_id;var main_category_icon;var get_cat_icon;var cat_media_holder;
		var removeimage;var cat_image_id;var imagelbl;var get_cat_icon_id;
		jQuery(document).on('change','.pgs-woo-api_main_category',function(){
			get_cat_id = jQuery(this).val();
			get_cat_icon = jQuery(this).find(':selected').attr('data-caticon');
			get_cat_icon_id = jQuery(this).find(':selected').attr('data-caticonid');
			main_category_icon = jQuery(this).parents('.pgs-woo-api-field-group').find('.pgs-woo-api_main_category_icon');
			cat_media_holder = jQuery(main_category_icon).find('.upload_image');
			removeimage = jQuery(main_category_icon).find('.remove-image-button');
			cat_image_id = jQuery(main_category_icon).find('.upload_image_id');
			imagelbl = jQuery(main_category_icon).find('.upload-image-button');
			if(get_cat_id){
				if(get_cat_icon != ''){
					var img = '<img src="' + get_cat_icon + '" width="150px" height="150px" />';
					jQuery(removeimage).show();
					jQuery(cat_image_id).val(get_cat_icon_id);
					jQuery(imagelbl).text("Edit");
				} else {
					var img = '';
					jQuery(removeimage).hide();
					jQuery(cat_image_id).val('');
					jQuery(imagelbl).text("Add Image");
				}
				jQuery(cat_media_holder).html(img);
				jQuery(main_category_icon).show();
			} else {
				jQuery(main_category_icon).hide();
			}
		});
		/** End **/

		/**
		* Add Datepiker on user date of birth field
		*/
		if(document.getElementById('dob')){
			jQuery( "#dob" ).datepicker({
			  dateFormat: "mm/dd/yy"
			});
		}
		/** End **/

		/*
		* Open filte upload onclick button
		*/
		if(document.getElementById('open_pem_file_dev')){
			jQuery('#open_pem_file_dev').on('click',function(event){
				event.preventDefault();
				jQuery('#pem_file_dev').trigger('click');
			});

			jQuery(document).on('change','#pem_file_dev',function() {
				var dev_file = this.files && this.files.length ? this.files[0].name : '';
				jQuery('#pem-dev-file-desc').text( dev_file );
			});

		}
		if(document.getElementById('open_pem_file_pro')){

			jQuery('#open_pem_file_pro').on('click',function(event){
				event.preventDefault();
				jQuery('#pem_file_pro').trigger('click');
			});

			jQuery(document).on('change','#pem_file_pro',function() {
				var pro_file = this.files && this.files.length ? this.files[0].name : '';
				jQuery('#pem-pro-file-desc').text(pro_file);
			});
		}
		/** End **/


		/**
		*
		* WP Media Upload popup box
		*
		*/
		var mediaUploader;var $this;
		jQuery(document).on( 'click','.upload-image-button', function ( event ) {
			event.preventDefault();
			$this = jQuery( this ),
				current_parent = jQuery(this).closest('.pgs-woo-api-field-group');
				media_holder = jQuery(current_parent).find('.upload_image');
				hd_image_id = jQuery(current_parent).find('.upload_image_id');

			// If the uploader object has already been created, reopen the dialog
			if (mediaUploader) {
				mediaUploader.open();
				return;
			}
			// Extend the wp.media object
			mediaUploader = wp.media.frames.file_frame = wp.media({
				title: pgs_woo_api.choose_image,
				button: {
				text: pgs_woo_api.choose_image,
			}, multiple: false });

			// When a file is selected, grab the URL and set it as the text field's value
			mediaUploader.on('select', function() {
				var attachment = mediaUploader.state().get('selection').first().toJSON();
				jQuery(current_parent).find('.upload-image-button').removeClass('button button-primary').addClass('button button-default').html('Edit');
				var img = '<img src="' + attachment.url + '" width="150px" height="150px" />';
				jQuery(media_holder).html(img);
				jQuery(hd_image_id).val( attachment.id );
				jQuery(current_parent).find('.remove-image-button').show();
			});
			// Open the uploader dialog
			mediaUploader.open();
		});

		//Remove image on click remove button
		jQuery(document).on( 'click', '.remove-image-button', function( event ) {
			event.preventDefault();
			var $this = jQuery( this );
			$this.parent().find('.upload_image').html( '' );
			$this.parent().find('.upload-image-button').removeClass('button button-default').html('Edit');
			$this.parent().find('.upload-image-button').addClass('button button-primary').html(pgs_woo_api.add_image);

			$this.parent().find('.remove-image-button').hide();
			$this.parent().find('.upload_image_id').val( 0 );
		} );
		/** End **/

		// Add Color Picker to all inputs that have 'color-field' class
		jQuery('.cpa-color-picker').wpColorPicker();


		/*
		* Vertical tabs
		*/
		if(document.getElementById('pgs-woo-api-tabs')){
			var index = 'pgs-woo-api-active-tab';
			//  Define friendly data store name
			var store_data = window.sessionStorage;
			var old_index = 0;
			//  Start magic!
			try {
				// getter: Fetch previous value
				old_index = store_data.getItem(index);
			} catch(e) {}
			var pgsTabs = jQuery( "#pgs-woo-api-tabs" ).tabs({
				active: old_index,
				activate: function(event, ui) {
					//  Get future value
					var new_index = ui.newTab.parent().children().index(ui.newTab);
					//  Set future value
					try {
						store_data.setItem( index, new_index );
						old_id = ui.newPanel.attr('id');
					} catch(e) {}
				}
			}).addClass( "ui-tabs-vertical ui-helper-clearfix" );
			jQuery( "#pgs-woo-api-tabs li" ).removeClass( "ui-corner-top" ).addClass( "ui-corner-left" );
		}
		/** End **/


		/*
		* Repeater Field
		*/
		'use strict';
		jQuery('#pgs-woo-api-main_slider').repeater({
			show: function () {
				jQuery('.upload_image', this).html('');
				jQuery('.upload-image-button', this).removeClass('button button-default').html('Edit');
				jQuery('.upload-image-button', this).addClass('button button-primary').html(pgs_woo_api.add_image);
				jQuery('.remove-image-button', this).hide();
				jQuery(this).slideDown();
			},
			hide: function (deleteElement) {
				//if(confirm(pgs_woo_api.delete_msg)) {
					jQuery(this).slideUp(deleteElement);
				//}
			},
			ready: function (setIndexes) {
			}
		});

		jQuery('#pgs-woo-api-category-banners,#pgs-woo-api-banner-ad,#pgs-woo-api-feature-box').repeater({
			show: function () {
				jQuery('.upload_image', this).html('');
				jQuery('.upload-image-button', this).removeClass('button button-default').html('Edit');
				jQuery('.upload-image-button', this).addClass('button button-primary').html(pgs_woo_api.add_image);
				jQuery('.remove-image-button', this).hide();
				jQuery(this).slideDown();
			},
			hide: function (deleteElement) {
				//if(confirm(pgs_woo_api.delete_msg)) {
					jQuery(this).slideUp(deleteElement);
				//}
			},
			ready: function (setIndexes) {
			}
		});

		jQuery('#pgs-woo-api-info-pages,#pgs-woo-api-main-category,#pgs-woo-api-is-web-view').repeater({
			initEmpty: false,
			show: function () {
				jQuery('.upload_image', this).html('');
				jQuery('.upload-image-button', this).removeClass('button button-default').html('Edit');
				jQuery('.upload-image-button', this).addClass('button button-primary').html(pgs_woo_api.add_image);
				jQuery('.remove-image-button', this).hide();
				jQuery(this).slideDown();

				if( jQuery(this).parents("#pgs-woo-api-main-category").attr("data-limit").length > 0 ){
					if( jQuery(this).parents("#pgs-woo-api-main-category").find("div[data-repeater-item]").length <= jQuery(this).parents("#pgs-woo-api-main-category").attr("data-limit") ){
						jQuery(this).slideDown();
					} else {
						jQuery(this).remove();
					}
				} else {
					jQuery(this).slideDown();
				}

			},
			hide: function (deleteElement) {
				//if(confirm(pgs_woo_api.delete_msg)) {
					jQuery(this).slideUp(deleteElement);
				//}
			},
			ready: function (setIndexes) {
			}
		});

		/*
		* Remove app cat image on add new image with ajax
		*/
		jQuery('input[name=submit]').on( 'click', function() {
			setTimeout(function(){
				jQuery( '#product_app_cat_thumbnail' ).find( 'img' ).attr( 'src', pgs_woo_api.pgs_api_url+'img/pgs_app_cat_placeholder.jpg' );
				jQuery('#product_app_cat_thumbnail_id').val( 0 );
				jQuery( '.remove_app_image_button' ).hide();
			},2000);
		} );
		/** End **/


		/**
		* Second media Popup box for App cat image
		*/
		var mediaUploader;
		jQuery(document).on( 'click','#upload-image-button', function ( event ) {
			event.preventDefault();
			var upload_btn        = this,
				cat_thumb_wrapper = jQuery( upload_btn ).closest('.ciyashop-app-product-cat-thumb-wrapper'),
				media_holder      = jQuery( cat_thumb_wrapper ).find( '.upload_image' ),
				upload_image_el   = jQuery( cat_thumb_wrapper ).find( '.upload_image_id' ),
				del_btn           = jQuery( cat_thumb_wrapper ).find( '.remove_app_image_button' );

			// If the uploader object has already been created, reopen the dialog
			if ( mediaUploader ) {
				mediaUploader.open();
				return;
			}
			// Extend the wp.media object
			mediaUploader = wp.media.frames.file_frame = wp.media({
				title: pgs_woo_api.choose_image,
				button: {
				text: pgs_woo_api.choose_image
			}, multiple: false });

			// When a file is selected, grab the URL and set it as the text field's value
			mediaUploader.on('select', function() {
				var attachment = mediaUploader.state().get('selection').first().toJSON();
				var img        = '<img src="' + attachment.url + '" width="60px" height="60px" />';
				$( media_holder ).html( img );
				$( upload_image_el ).val( attachment.id );
				$( del_btn ).show();
			});
			// Open the uploader dialog
			mediaUploader.open();
		});

		jQuery(document).on( 'click', '#remove-image-button', function( event ) {
			event.preventDefault();
			var del_btn           = this,
				cat_thumb_wrapper = jQuery( del_btn ).closest('.ciyashop-app-product-cat-thumb-wrapper'),
				upload_image_el   = jQuery( cat_thumb_wrapper ).find( '.upload_image_id' );

			current_parent = jQuery(this).closest('.form-field');
			jQuery( cat_thumb_wrapper ).find( 'img' ).attr( 'src', pgs_woo_api.pgs_api_url + 'img/pgs_app_cat_placeholder.jpg' );
			jQuery( upload_image_el ).val( 0 );
			jQuery( del_btn ).hide();
		} );
		/** End **/

		/**
		* Call test api
		*/
		if(document.getElementById('test-api-btn')){
			jQuery(document).on('click','#test-api-btn',function(){
				jQuery.ajax({
					url: ajaxurl,
					type: 'post',
					dataType: 'json',
					data:{action:'pgs_woo_api_test_api_ajax_call'},
					beforeSend: function(){
						jQuery('.pgs-loader').addClass('pgs-api-console');
						jQuery('.pgs-loader').html('loading..');
					},
					success: function(response){
						jQuery('.pgs-loader').html(response);
					},
					error: function(msg){
						alert('Something went wrong!');
						jQuery('.pgs-loader').removeClass('pgs-api-console');
						jQuery('.pgs-loader').html('');

					}
				});
			});
		}

		/**
		* Submit token generations proccess form fields to ajax
		*/
		jQuery(document).on('click','.token-gen-pro',function(){
			var id = jQuery(this).attr('id');
			var form_data = '';
			if(id == "stp-1"){
				//var form_data = $(".step-1 :input");
				form_data += '<input type="hidden" name="step" value="1" />';
			} else if(id == "stp-2"){
				var oauth_consumer_key = jQuery("input[name=oauth_consumer_key]").val();
				var oauth_consumer_secret = jQuery("input[name=oauth_consumer_secret]").val();
				var oauth_token = jQuery("input[name=oauth_token]").val();
				var oauth_token_secret = jQuery("input[name=oauth_token_secret]").val();
				var oauth_verifier = jQuery("input[name=oauth_verifier]").val();

				form_data += '<input type="hidden" name="oauth_consumer_key" value="'+oauth_consumer_key+'" />';
				form_data += '<input type="hidden" name="oauth_consumer_secret" value="'+oauth_consumer_secret+'" />';
				form_data += '<input type="hidden" name="oauth_token" value="'+oauth_token+'" />';
				form_data += '<input type="hidden" name="oauth_token_secret" value="'+oauth_token_secret+'" />';
				form_data += '<input type="hidden" name="oauth_verifier" value="'+oauth_verifier+'" />';
				form_data += '<input type="hidden" name="step" value="2" />';
			}

			jQuery('<form>', {
				"id": "getPgsApiData",
				"html": form_data,
				"action": ''
			}).appendTo(document.body).submit();
		});

		/**
		* On update option or settings show loader
		*/
		if(document.getElementById('geo-publish-btn')){
			jQuery('#geo-publish-btn').on('click',function(){
				jQuery('.spinner').css('visibility','visible');
			});
		}

		/**
		* Display device images based on device selected
		*/
		if(document.getElementsByClassName('pgs-woo-device-img-display')){
			jQuery('.pgs-woo-device-img-display').on('click',function(e){
				e.preventDefault();
				jQuery(this).parents(".pgs-woo-api-panel-sidebar").find('.pgs-woo-device-img-display').removeClass("active");
				jQuery(this).addClass("active");
				jQuery(this).parents(".pgs-woo-api-panel-sidebar").find('.device-display').addClass('hidden');
				jQuery("#"+ jQuery(this).data('target')).removeClass('hidden');
			});
		}
		jQuery('.credentials-code-device-img').on('click', function (e) {
			e.preventDefault();

			jQuery('.credentials-code-device-img').removeClass('active');
			jQuery(this).addClass('active');

			var html_cd             = '',
				credentials_code    = pgs_app_sample_data_import_object.something_went_wrong,
				client_key          = jQuery('input.client_key').val(),
				client_secret       = jQuery('input.client_secret').val(),
				token               = jQuery('input.token').val(),
				token_secret        = jQuery('input.token_secret').val(),
				consumer_key        = jQuery('input.consumer_key').val(),
				consumer_secret     = jQuery('input.consumer_secret').val(),
				site_url            = jQuery('input.pgs-site-url').val(),
				plugin_varsion      = pgs_app_sample_data_import_object.plugin_ver,
				android_purchased   = pgs_app_sample_data_import_object.purchased_android,
				ios_purchased       = pgs_app_sample_data_import_object.purchased_ios,
				purchasekey_android = pgs_app_sample_data_import_object.purchasekey_android,
				purchasekey_ios     = pgs_app_sample_data_import_object.purchasekey_ios;

			if(client_key == '' || client_secret == '' || token == '' || token_secret == '' || consumer_key == '' || consumer_secret == '') {
				credentials_code    = pgs_app_sample_data_import_object.something_went_wrong,
				jQuery('#credentials-code-api-btn').hide();
			} else if(jQuery(this).data('target') == 'credentials-code-android') {
				html_cd = '';
				if(android_purchased){
					html_cd += 'public final String APP_URL = "'+ site_url +'/";\n';
					html_cd += 'public final String WOO_MAIN_URL = APP_URL + "wp-json/wc/v2/";\n';
					html_cd += 'public final String MAIN_URL = APP_URL + "wp-json/pgs-woo-api/v1/";\n\n';
					html_cd += 'public static final String CONSUMERKEY = "'+ client_key +'";\n';
					html_cd += 'public static final String CONSUMERSECRET = "'+ client_secret +'";\n';
					html_cd += 'public static final String OAUTH_TOKEN = "'+ token +'";\n';
					html_cd += 'public static final String OAUTH_TOKEN_SECRET = "'+ token_secret +'";\n\n';
					html_cd += 'public static final String WOOCONSUMERKEY = "'+ consumer_key +'";\n';
					html_cd += 'public static final String WOOCONSUMERSECRET = "'+ consumer_secret +'";\n';
					html_cd += 'public static final String version = "'+ plugin_varsion +'";\n';
					html_cd += 'public static final String purchasekey = "'+ purchasekey_android +'";';
					jQuery('#credentials-code-api-btn').show();
				} else {
					html_cd += 'Please validate item purchase code. App Settings > Support';
				}
				credentials_code = html_cd;
			} else if(jQuery(this).data('target') == 'credentials-code-ios') {
				html_cd = '';
				if(ios_purchased){
					html_cd += 'let OAUTH_CUSTOMER_KEY : String = "'+ consumer_key +'" \n';
					html_cd += 'let OAUTH_CUSTOMER_SERCET : String = "'+ consumer_secret +'" \n';
					html_cd += 'let OAUTH_CONSUMER_KEY_PLUGIN : String = "'+ client_key +'"\n';
					html_cd += 'let OAUTH_CONSUMER_SECRET_PLUGIN : String = "'+ client_secret +'"\n';
					html_cd += 'let OAUTH_TOKEN_PLUGIN : String = "'+ token +'"\n';
					html_cd += 'let OAUTH_TOKEN_SECRET_PLUGIN : String = "'+ token_secret +'"\n\n';
					html_cd += 'let appURL : String = "'+ site_url +'/"\n';
					html_cd += 'let PATH : String = appURL + "wp-json/wc/v2/"\n';
					html_cd += 'let OTHER_API_PATH : String = appURL + "wp-json/pgs-woo-api/v1/"\n';
					html_cd += 'let PLUGIN_VERSION : String = "'+ plugin_varsion +'"\n';
					html_cd += 'let PURCHASEKEY : String = "'+ purchasekey_ios +'";';
					jQuery('#credentials-code-api-btn').show();
				} else {
					html_cd += 'Please validate item purchase code from App Settings > Support';
				}
				credentials_code = html_cd;
			}
			jQuery('.pgs-woo-api-credentials-code').removeClass('pgs-hidden');
			jQuery('.pgs-woo-api-credentials-code').text(credentials_code);
		});

		jQuery('#credentials-code-api-btn').on('click', function(e) {
			e.preventDefault();
			jQuery('.pgs-woo-api-credentials-code').select();
			document.execCommand("copy");
		});

		jQuery( "#htaccess-code-toggle" ).click(function() {
		  jQuery( "#htaccess-code" ).toggle( function() {
			// Animation complete.
		  });
		});



		jQuery( '.pgs-woo-api-import-this-sample' ).click( function( e ) {
			e.preventDefault();
			//alert('sdfsdf');
			if( jQuery(this).hasClass('disabled') ){
				return false;
			}

			var current_element = jQuery(e.target);

			if( current_element.data('message') ){
				var import_message = unescape(current_element.data('message'));
			}else{
				var import_message = pgs_app_sample_data_import_object.alert_default_message;
			}

			var install_required_plugins = false;
			if( current_element.data('required-plugins') ){
				install_required_plugins = true;
			}

			var template = wp.template( 'pgs-woo-api-sample-import-alert' );
			var template_content = template( {
				title: current_element.data('title'),
				message: import_message,
				//import_requirements_list: sample_data_import_object.sample_data_requirements,
				required_plugins_list: pgs_app_sample_data_import_object.sample_data_required_plugins_list
			});

			jQuery.confirm({
				title: pgs_app_sample_data_import_object.alert_title,
				content: template_content,
				type: 'red',
				icon: 'fa fa-warning',
				animation: 'scale',
				closeAnimation: 'scale',
				bgOpacity: 0.8,
				columnClass: 'col-md-6 col-md-offset-3 col-sm-8 col-sm-offset-2 col-xs-10 col-xs-offset-1 sample-data-confirm',
				buttons: {
					'confirm': {
						text: pgs_app_sample_data_import_object.alert_proceed,
						btnClass: 'btn-green',
						action: function () {
							if( install_required_plugins ){
								window.location = pgs_app_sample_data_import_object.tgmpa_url;
							}else{

								// ********************************** Ajax Start **********************************

								var sample_import_nonce = jQuery('#sample_import_nonce').val();

								var data = {
									action: 'pgs_woo_api_plugin_import_sample', //calls wp_ajax_nopriv_ajaxlogin
									sample_id: current_element.data('id'),
									sample_import_nonce: pgs_app_sample_data_import_object.sample_import_nonce,
									action_source: 'plugin-options',
								};

								jQuery.ajax({
									type: 'POST',
									dataType: 'json',
									url: pgs_app_sample_data_import_object.ajaxurl,
									data: data,
									beforeSend: function( xhr ) {
										jQuery('#pgs-woo-api-panel-body-loader').parent().addClass('overlay-loader');
										jQuery('#pgs-woo-api-panel-body-loader').html('<span class="sample-data-loader">Loading..</span>');
										//overlay-loader
									},
									success: function(data){
										// Hide Loader
										//jQuery(loader).removeClass('is-active');

										// Hide Overlay
										//overlay.fadeOut( 'fast' );

										if( data.success ){
											jQuery('.data-alert-notitication').html(data.message).slideDown('slow').delay(5000).slideUp('slow');
											jQuery('.data-alert-notitication').html(data.alert_msg).delay(500).slideDown('slow').delay(15000).slideUp('slow');

											// Reload Page
											window.setTimeout(function(){
												jQuery('#pgs-woo-api-panel-body-loader').html('');
												jQuery('#pgs-woo-api-panel-body-loader').parent().removeClass('overlay-loader');
												document.location.href = document.location.href;
											}, 5000);
										}else{
											jQuery('#pgs-woo-api-panel-body-loader').html('');
											jQuery('#pgs-woo-api-panel-body-loader').parent().removeClass('overlay-loader');
											//jQuery('.data-alert-notitication').html(data.alert_msg).slideDown('slow').delay(5000).slideUp('slow');
											jQuery('.data-alert-notitication').html(data.alert_msg);
										}
										return data;
									}
								});
								//**********************************  Ajax End  **********************************

							}
						}
					},
					'cancel': {
						text: pgs_app_sample_data_import_object.alert_cancel,
						btnClass: 'btn-red',
					},
				},
				onContentReady: function () {
					if( install_required_plugins ){
						this.buttons.confirm.setText(pgs_app_sample_data_import_object.alert_install_plugins);
					}
				},
				onOpen: function () {
					// $.alert('onOpen');
				},
			});
		});


		/**
		* Custom Css Editor JS Code
		*/
		if(document.getElementById('pgs_woo_api_custom_css_editor')){
			var editor = ace.edit("pgs_woo_api_custom_css_editor");
			var code = editor.getValue();
			editor.session.setMode( "ace/mode/css" );

			var input = jQuery('input[name="pgs_woo_api_checkout_custom_css"]');
				editor.getSession().on("change", function () {
				input.val(editor.getSession().getValue());
			});
		}

		/**
		* WhatsUpp phone number validation
		*/
		if(document.getElementById('pgs-woo-api-whatsapp-no-validation')){
			jQuery('#pgs-woo-api-whatsapp-no-validation').on("input", function(e) {
				if (!pgs_woo_api_validate_phone('pgs-woo-api-whatsapp-no-validation')) {
					jQuery('#validation_status').html('Invalid');
					jQuery('#validation_status').css('color', 'red');
				} else {
					jQuery('#validation_status').html('');
					//jQuery('#validation_status').css('color', 'green');
				}
			});
		}
		/**
		* WhatsUpp phone number validation rules
		*/
		function pgs_woo_api_validate_phone(phone_txt) {
			var phone_val = document.getElementById(phone_txt).value;
			var valid_rules1 = patt1 = /\++/g;
			var valid_rules2 = /^((\+[1-9]{1,4}[ \-]*)|(\([0-9]{2,3}\)[ \-]*)|([0-9]{2,4})[ \-]*)*?[0-9]{3,4}?[ \-]*[0-9]{3,4}?$/;
			if ( valid_rules1.test(phone_val) && valid_rules2.test(phone_val) ) {
				return true;
			}
			else {
				return false;
			}
		}

		/**
		* Show / Hide options
		*/
		jQuery('.checkout-multistep-button-status').on('click',function(){
			var status = jQuery(this).val();
			if(status == "disable"){
				jQuery('#pgs_woo_api_multi_num_of_steps_button-groups').hide();
				jQuery('#checkout-android-section').find('img').attr("src",pgs_app_sample_data_import_object.checkout_imge_url_admin+'/android/checkout.png');
				jQuery('#checkout-ios-section').find('img').attr("src",pgs_app_sample_data_import_object.checkout_imge_url_admin+'/ios/checkout.png');
			} else {
				jQuery('#pgs_woo_api_multi_num_of_steps_button-groups').show();
				jQuery('#checkout-android-section').find('img').attr("src",pgs_app_sample_data_import_object.checkout_imge_url_admin+'/android/multistep.png');
				jQuery('#checkout-ios-section').find('img').attr("src",pgs_app_sample_data_import_object.checkout_imge_url_admin+'/ios/multistep.png');
			}
		});

		/** End **/
		if(document.getElementById('pgs_woo_api_multi_num_of_steps_button-groups')){
			jQuery(".pgs_woo_api_multi_num_of_steps_button").on('click',function() {
				if(jQuery(this).attr('data-step') == 4){
					jQuery("#pgs-img-steps-3").removeClass('image-select-selected');
					jQuery("#pgs-img-steps-4").addClass('image-select-selected');
				} else {
					jQuery("#pgs-img-steps-4").removeClass('image-select-selected');
					jQuery("#pgs-img-steps-3").addClass('image-select-selected');
				}

			});
		}


		if(document.getElementById('pgs-woo-api-custom-section-product')){
			setTimeout(function(){
				jQuery('.pgs-woo-api-custom-section-product').find('select').each(function() {
					var $this = jQuery(this);
					get_more_product($this);
				});
			}, 100);

			jQuery('.pgsaddcfield, .pgsremovecfield').on('click', function() {
				setTimeout(function(){
					jQuery('.pgs-woo-api-custom-section-product').find('select').each(function() {
						var $this = jQuery(this);
						get_more_product($this);
					});
				}, 100);
			});
		}

		function get_more_product($this){
			jQuery($this).select2({
				ajax: {
					url: ajaxurl+ "?action=pgs_woo_api_get_customsection_products",
					//type: 'POST',
					dataType: 'json',
					delay: 250,
					placeholder: 'Breed...',
					allowClear: true,
					data:function (params) {
						return {
							search: params.term,
							page: params.page || 1,
						};
					},
					processResults: function (data) {
						data.page = data.page || 1;
						return {
							results: data.items,
							pagination: {
								more: data.pagination
							}
						}
					},
					cache: true,
				},
				placeholder: 'Search for products',
				templateResult: formatPro,
				templateSelection: formatproSelection
			});
		}

		function formatPro (pro) {
			if (pro.loading) {
			return pro.text;
			}

			var $container = $(
			"<div class='select2-result-repository clearfix'>" +
				"<div class='select2-result-repository__avatar'><img src='" + pro.img + "' width='50px' height='50px' /></div>" +
				"<div class='select2-result-repository__meta'>" +
					"<div class='select2-result-repository__title'></div>" +
					"<div class='select2-result-repository__description'></div>" +
				"</div>" +
			"</div>"
			);
			$container.find(".select2-result-repository__title").text(pro.text);
			$container.find(".select2-result-repository__description").html(pro.price);
			return $container;
		}

		function formatproSelection (pro) {
		return pro.text;
		}
	});
})( jQuery );
