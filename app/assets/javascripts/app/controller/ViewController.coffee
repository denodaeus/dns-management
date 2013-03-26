Ext.define( 'DnsManagement.controller.ViewController'
	extend: 'Deft.mvc.ViewController'
	mixins: [ 'Deft.mixin.Injectable' ]
	inject: [ 'recordDataJson' ]

	control:
		recordGrid:
			selectionchange: 'onRecordGridSelectionChange'

		recordDetail: true
		record: true

	config:
		recordDataJson: null

	init: ->
		@callParent( arguments )

	onRecordGridSelectionChange: ( record, records ) ->
		recordDataModel = records[0]
		@getRecordDetail().update(recordDataModel.getData())
		@getRecordDataJson().loadData(recordDataModel.getData())

)