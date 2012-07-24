(function($, cloudStack) {
  var elems = {
    inputArea: function(args) {
      var $form = $('<form>').addClass('tag-input');
      var $keyField = $('<div>').addClass('field key');
      var $keyLabel = $('<label>').attr('for', 'key').html('Key:');
      var $key = $('<input>').addClass('key required').attr('name', 'key');
      var $valueField = $('<div>').addClass('field value');
      var $valueLabel = $('<label>').attr('for', 'value').html('Value:');
      var $value = $('<input>').addClass('value required').attr('name', 'value');
      var $submit = $('<input>').attr('type', 'submit').val('Add');

      $keyField.append($keyLabel, $key);
      $valueField.append($valueLabel, $value);
      $form.append(
        $keyField, $valueField,
        $submit
      );

      $form.validate({ onfocusout: false });

      $form.submit(
        args.onSubmit ?
          function() {
            if (!$form.valid()) return false;
            
            args.onSubmit({
              data: cloudStack.serializeForm($form),
              response: {
                success: function() {
                  // Restore editing of input
                  $key.attr('disabled', false);
                  $value.attr('disabled', false);

                  // Clear out old data
                  $key.val(''); $value.val('');
                  $key.focus();
                },
                error: function() {
                  // Restore editing of input
                  $key.attr('disabled', false);
                  $value.attr('disabled', false);
                  $key.focus();
                }
              }
            });
            
            // Prevent input during submission
            $key.attr('disabled', 'disabled');
            $value.attr('disabled', 'disabled');
            
            return false;
          } :
        function() { return false; }
      );

      return $form;
    },
    tagItem: function(title, onRemove, data) {
      var $li = $('<li>');
      var $label = $('<span>').addClass('label').html(title);
      var $remove = $('<span>').addClass('remove').html('X');

      $remove.click(function() {
        if (onRemove) onRemove($li, data);
      });

      $li.append($remove, $label);
      
      return $li;
    },

    info: function(text) {
      var $info = $('<div>').addClass('tag-info');
      var $text = $('<span>').html(text);

      $text.appendTo($info);

      return $info;
    }
  };
  
  $.widget('cloudStack.tagger', {
    _init: function(args) {
      var context = this.options.context;
      var dataProvider = this.options.dataProvider;
      var actions = this.options.actions;
      var $container = this.element.addClass('tagger');
      var $tagArea = $('<ul>').addClass('tags');
      var $title = elems.info('Tags').addClass('title');
      var $loading = $('<div>').addClass('loading-overlay');

      var onRemoveItem = function($item, data) {
        $loading.appendTo($container);
        actions.remove({
          context: $.extend(true, {}, context, {
            tagItems: [data]
          }),
          response: {
            success: function(args) {
              var notification = $.extend(true, {} , args.notification, {
                interval: 500,
                _custom: args._custom
              });
              
              cloudStack.ui.notifications.add(
                notification,

                // Success
                function() {
                  $loading.remove();
                  $item.remove();
                }, {},

                // Error
                function() {
                  $loading.remove();
                }, {}
              );
            },
            error: function(message) {
              $loading.remove();
              cloudStack.dialog.notice({ message: message });
            }
          }
        });
      };
      
      var $inputArea = elems.inputArea({
        onSubmit: function(args) {
          var data = args.data;
          var success = args.response.success;
          var error = args.response.error;
          var title = data.key + ' = ' + data.value;

          $loading.appendTo($container);
          actions.add({
            data: data,
            context: context,
            response: {
              success: function(args) {
                var notification = $.extend(true, {} , args.notification, {
                  interval: 500,
                  _custom: args._custom
                });

                cloudStack.ui.notifications.add(
                  notification,

                  // Success
                  function() {
                    $loading.remove();
                    elems.tagItem(title, onRemoveItem, data).appendTo($tagArea);
                    success();  
                  }, {},

                  // Error
                  function() {
                    $loading.remove();
                    error();
                  }, {}
                );                
              },
              error: function(message) {
                $loading.remove();
                error();
                cloudStack.dialog.notice({ message: message });
              }
            }
          });
        }
      });

      $container.append($title, $inputArea, $tagArea);

      // Get data
      $loading.appendTo($container);
      dataProvider({
        context: context,
        response: {
          success: function(args) {
            var data = args.data;
            
            $loading.remove();
            $(data).map(function(index, item) {
              var key = item.key;
              var value = item.value;
              var data = { key: key, value: value };

              elems.tagItem(key + ' = ' + value, onRemoveItem, data).appendTo($tagArea);
            });
          },
          error: function(message) {
            $loading.remove();
            $container.find('ul').html(message);
          }
        }
      });
    }
  });
}(jQuery, cloudStack));