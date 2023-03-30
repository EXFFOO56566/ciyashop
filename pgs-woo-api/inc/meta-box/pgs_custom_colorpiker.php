<?php
add_action( 'pa_color_add_form_fields', 'pgs_woo_api_taxonomy_add_new_pa_color_meta_field', 40, 2 );
add_action( 'pa_color_edit_form_fields', 'pgs_woo_api_taxonomy_edit_new_pa_color_meta_field',40,2 );

add_action( 'create_pa_color', 'save_pa_color_custom_tax_field' );
add_action( 'edited_pa_color', 'save_pa_color_custom_tax_field' );

// Add term page
function pgs_woo_api_taxonomy_add_new_pa_color_meta_field($term) {
	// this will add the custom meta field to the add new term page
    $vsrc = array();
    if(isset($term->term_id) && !empty($term->term_id)){
        $termID = $term->term_id;
    	$color_code = $termMeta['color_code'];


    }
    ob_start();
    ?>
	<div class="form-field term-thumbnail-wrap">
        <label><?php esc_html_e('Color Code','pgs-woo-api')?></label>
        <div id="product_app_cat_thumbnail" style="float: left; margin-right: 10px;">
            <input type="text" id="color_code" class="cpa-color-picker" name="color_code">
        </div>
        <div class="clear"></div>
    </div>
    <?php
    $output = ob_get_contents();
    ob_end_clean();
    echo $output;

}


function pgs_woo_api_taxonomy_edit_new_pa_color_meta_field($term) {
	// this will add the custom meta field to the add new term page
    $color_code = '';
    if(isset($term->term_id) && !empty($term->term_id)){
        $termID = $term->term_id;
    	$color_code = get_term_meta($termID, 'color_code', true);
    }
    ob_start();
    ?>
	<tr class="form-field">
    	<th scope="row" valign="top"><label><?php esc_html_e('Color Code','pgs-woo-api')?></label></th>
    	<td>
            <div id="product_app_cat_thumbnail" style="float: left; margin-right: 10px;">
                <input type="text" id="color_code" class="cpa-color-picker" name="color_code" value="<?php echo $color_code?>">
            </div>
            <div class="clear"></div>
    	</td>
    </tr>
    <?php
    $output = ob_get_contents();
    ob_end_clean();
    echo $output;

}

function save_pa_color_custom_tax_field( $termID ) {

    if ( isset( $_POST['color_code'] ) ) {
		$termMeta['color_code'] = isset( $_POST['color_code'] ) ? $_POST['color_code'] : '';
        update_term_meta($termID, 'color_code', $termMeta['color_code']);
	}
}



/* ---------------------------------------------------------------------------
 * Edit columns
 * --------------------------------------------------------------------------- */
function pgs_woo_api_app_color_columns($columns)
{
	$newcolumns = array(
		"cb" => "<input type='checkbox' />",
		"app_color" => esc_html__( 'Color', 'pgs-woo-api' ),
	);
	$columns = array_merge($newcolumns, $columns);

	return $columns;
}
add_filter("manage_edit-pa_color_columns", "pgs_woo_api_app_color_columns",10,1);
/* ---------------------------------------------------------------------------
 * Custom columns
 * --------------------------------------------------------------------------- */
function pgs_woo_api_pa_color_add_columns($content, $column_name, $term_id) {

    $data = get_term( $term_id);
    $text_color = 'color:#fff;';
    if($data->slug == 'white'){
        $text_color = 'color:#000;border:solid 1px #f1f1f1;';
    }
    $vsrc = array();
    if($column_name == "app_color"){
        if(isset($term_id) && !empty($term_id)){
            $termID = $term_id;
        	$color_code = get_term_meta($termID, 'color_code', true);

            $color = '';
            if(!empty($color_code)){
                $color = '<span style="padding-left:2px;padding-right:4px;'.esc_attr($text_color).'background-color:'.esc_attr($color_code).'">'.esc_attr($color_code).'</span>';
            }
            return $color;
        }
    }
}
add_filter( 'manage_pa_color_custom_column', 'pgs_woo_api_pa_color_add_columns', 10, 3 );