Ext.define( 'DnsManagement.view.RecordPanel',
  extend: 'Ext.grid.Panel'
  mixins: [ 'Deft.mixin.Controllable', 'Deft.mixin.Injectable' ]
  inject: [ 'recordDataJson' ]
  controller: 'DnsManagement.controller.ViewController'


  frame: true
  height: 667
  id: 'DnsManagement'
  width: 500
  autoScroll: true
  layout:
    align: 'stretch'
    type: 'vbox'
  title: 'Records'


  initComponent: ->
    Ext.applyIf( @,

      items: [
        xtype: 'gridpanel'
        itemId: 'recordGrid'
        store: this.recordDataJson
        flex: 1
        viewConfig: {}
        columns: [
          xtype: 'gridcolumn'
          dataIndex: 'id'
          text: 'ID'
        ,
          xtype: 'gridcolumn'
          dataIndex: 'name'
          text: 'Name'
        ,
          xtype: 'numbercolumn'
          dataIndex: 'domainId'
          text: 'domainId'
        ,
          xtype: 'gridcolumn'
          dataIndex: 'content'
          text: 'Content'
          flex: 1
        ]
      ]
    )

    @callParent( arguments )
)