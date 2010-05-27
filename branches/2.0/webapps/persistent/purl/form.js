Purl.PURLForm = {};
Purl.PURLForm.getTypes = function(partial, zoned) {
    var types = [];
    if (partial) {
        types.push("purl:PartialPURL");
    } else {
        types.push("purl:PURL");
    }
    if (zoned) {
        types.push("purl:ZonedPURL");
    }
    console.log(types.join(" "));
    return types.join(" ");
};

Purl.PURLForm.modifyForm = function(relType) {
    $('#purl-form tr.rel').hide();
    $('#purl-form tr.rel.'+relType).show();
    if ($('#purl-form tr.rel:visible input').length === 0) {
        var input = $('<input type="text" id="m_target" resource="" value="" rel="' + $('input[name=' + relType + ']').val() + '" />');
        input.bind('change', function() {
            $(this).attr('resource', $(this).val());
        });
        $('#purl-form tr.rel:visible td.value').append(input);
    }
};

Purl.PURLForm.modifyType = function(type) {
    $('#purl-form').attr('typeof', type);
};

Purl.PURLForm.initializeForm = function() {
    var frag = document.location.hash.substr(1);
    var qs = document.location.search.substr(1);
    if (frag.length === 0 && qs !== "edit") {
        frag = "alternate";
    } else {
        frag = $('#purl-form tr.rel input').filter(function() {
            return $(this).val() !== '';
        }).parents('tr.rel').attr('class').replace('rel ','');
        var types = [];
        var isDisabled = false;
        var isTombstoned = false;
        var isUnresolvable = false;
        $('input[name=rdf-type]').each(function() {
            var type = $(this).attr('resource').replace('http://persistent.name/rdf/2010/purl#', 'purl:')
            if (type === "purl:Unresolvable") {
                isUnresolvable = true;
            } else if (type === "purl:DisabledPURL") {
                isDisabled = true;
            } else if (type === "purl:TombstonedPURL") {
                isTombstoned = true;
            } else {
                types.push(type);
            }
        });
        if (types.length === 1) {
            $('input[value='+types[0]+']').attr('checked', 'checked');
        } else if (types.length === 2) {
            $('input[value~='+types[0]+'][value~='+types[1]+']').attr('checked', 'checked');
        }
        if (isUnresolvable) {
            // @@@denote near target
        }
        if (isDisabled) {
            $('#m_disabled').attr('checked', 'checked');
        }
        if (isTombstoned) {
            $('#m_tombstoned').attr('checked', 'checked');
        }
    }
    $('#purl-rel-type').bind('change', function() {
        Purl.PURLForm.modifyForm($(this).val());
    });
    $('#purl-rel-type').val(frag).trigger('change');
    $('#purl-form input.purl_type').bind('change', function() {
        var typeofs = Purl.PURLForm.getTypes($('#m_partial').attr('checked'), $('#m_zoned').attr('checked'));
        Purl.PURLForm.modifyType(typeofs);
        if (typeofs.indexOf("PartialPURL") > 0) {
            $('#purl-form tr.partial').show();
        } else {
            $('#purl-form tr.partial').hide();
        }
    });
    $('#purl-form input.purl_type').trigger('change');
    if ($('#m_disabled').length > 0) {
        $('#m_disabled').bind('change', function() {
            var types = $('#purl-form').attr('typeof');
            if ($(this).attr('checked')) {
                $('#purl-form').attr('typeof', types + ' purl:DisabledPURL');
            } else {
                $('#purl-form').attr('typeof', types.replace('purl:DisabledPURL', ''));
            }
        });
    }
    if ($('#m_tombstoned').length > 0) {
        $('#m_tombstoned').bind('change', function() {
            var types = $('#purl-form').attr('typeof');
            if ($(this).attr('checked')) {
                $('#purl-form').attr('typeof', types + ' purl:TombstonedPURL');
            } else {
                $('#purl-form').attr('typeof', types.replace('purl:TombstonedPURL', ''));
            }
        });
    }
    $('#purl-form').bind('submit', function() {
        $('#purl-form tr:hidden input').each(function() {
            $(this).removeAttr('about').removeAttr('resource').remove('content').val('');
        });
    });
    $('#domain-span').text($('#m_domain').attr('href'))
};

$(document).ready(function() {
    Purl.PURLForm.initializeForm();
});
