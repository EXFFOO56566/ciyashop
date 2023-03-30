<script type="text/html" id="tmpl-pgs-woo-api-sample-import-alert">
	<h3 class="sample-title"><?php echo esc_html('Sample Data', 'pgs-woo-api');?> : {{data.title}}</h3>
	{{data.message}}

	<# if ( data.required_plugins_list ) { #>
		<h3 class="required-plugins"><?php echo esc_html('Required Plugins', 'pgs-woo-api');?> : </h3>
		<p class="required-plugins-message"><?php echo esc_html('Please install/activate below required plugins before proceed to import.', 'pgs-woo-api');?></p>
		<ul class="required-plugins-list">
			<# _.each( data.required_plugins_list, function(res, index) { #>
				<li>- {{res}}</li>
			<# }) #>
		</ul>
	<# } #>
</script>
