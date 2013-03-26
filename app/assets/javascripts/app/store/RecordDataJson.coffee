Ext.define('DnsManagement.store.RecordsDataJson',
	extend: 'Ext.data.Store',
	mixins: ['Deft.mixin.Injectable']
	requires: ['DnsManagement.model.Record']
	inject: ['appConfig']

	constructor: ( cfg ) ->
		me = this
		cfg = cfg or {}

		me.callParent( [ Ext.apply(
			autoLoad: true,
			model: 'DnsManagement.model.Record'

			proxy:
				type: 'ajax'
				url: @appConfig.getEndpoint('records').url
				reader:
					type: 'json'
					root: @appConfig.getEndpoint('records').root
			, cfg)

		] )
)