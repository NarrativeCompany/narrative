import * as React from 'react';
import { compose, lifecycle } from 'recompose';
import { injectIntl, InjectedIntlProps } from 'react-intl';
import { PostMessages } from '../i18n/PostMessages';
import { DesktopEditorWrapper, MobileEditorWrapper } from '../styled/shared/post';

// Require Editor JS files.
import 'froala-editor/js/froala_editor.pkgd.min.js';
import 'froala-editor/js/plugins/draggable.min.js';
import 'froala-editor/js/third_party/embedly.min.js';
import 'froala-editor/js/plugins/image.min.js';

// Require Editor CSS files.
import 'froala-editor/css/froala_style.min.css';
import 'froala-editor/css/froala_editor.pkgd.min.css';
import 'froala-editor/css/themes/dark.min.css';
import 'froala-editor/css/plugins/draggable.min.css';
import 'froala-editor/css/third_party/embedly.min.css';
import 'froala-editor/css/plugins/image.min.css';

// Require Font Awesome.
import 'font-awesome/css/font-awesome.css';
import { FroalaEmbedlyStyle } from '../../routes/PostDetail/components/Post';
import * as DeepMerge from 'deepmerge';
import { themeScreenSize } from '../styled/theme';

// tslint:disable no-var-requires
const { default: FroalaEditor} = require('react-froala-wysiwyg');
const $ = require('jquery');

// tslint:disable no-string-literal
window['$'] = $;
// tslint:disable no-any
let mobileEditor: any;
let desktopEditor: any;
let currentModel: any;
// tslint:enable no-any

// tslint:disable no-any object-literal-shorthand
function isActive (this: any, cmd: {}) {
  const blocks = this.selection.blocks();
  let tag = 'N';

  if (blocks.length) {
    const blk = blocks[0];
    const defaultTag = this.html.defaultTag();

    if (blk.tagName.toLowerCase() !== defaultTag && blk !== this.el) {
      tag = blk.tagName;
    }
  }

  if (['LI', 'TD', 'TH'].indexOf(tag) >= 0) {
    tag = 'N';
  }

  return tag.toLowerCase() === cmd;
}

const headingButtonConfigs = {
  callback: function (this: any, cmd: {}) {
    if (cmd !== 'h1' && cmd !== 'h2') {
      this.paragraphFormat.apply('H2');
      return;
    }

    if (isActive.apply(this, [cmd])) {
      this.paragraphFormat.apply('N');
    } else {
      this.paragraphFormat.apply(cmd);
    }
  },
  undo: true,
  refreshAfterCallback: true,
  refresh: function ($btn: any) {
    $btn.toggleClass('fr-active', isActive.apply(this, [$btn.data('cmd')]));
  },
};

// Define custom buttons.
$.FroalaEditor.DefineIcon('h1', {NAME: '<strong style="font-size: 16px;">T</strong>', template: 'text'});
$.FroalaEditor.DefineIcon('h2', {NAME: '<strong style="font-size: 12px;">T</strong>', template: 'text'});

// Register commands for toolbar buttons
$.FroalaEditor.RegisterCommand('h1', {
  ...headingButtonConfigs,
  title: 'Heading 1',
});

$.FroalaEditor.RegisterCommand('h2', {
  ...headingButtonConfigs,
  title: 'Heading 2'
});

// Register commands for inline toolbar buttons
$.FroalaEditor.RegisterQuickInsertButton('thing', {
  ...headingButtonConfigs,
  icon: 'h1',
  title: 'Heading 1',
});

const sharedEditorConfig = {
  key: 'LB5D2B2C2sB4E4H4A15B3A7E6F2E4A4fzpmvD-13F4ocqvdrnD-13lG5dB-7j==',
  embedlyKey: 'ee4bd8e093494fe4b822d7262c6093d5',
  shortcutsHint: true,
  pastePlain: true,
  listAdvancedTypes: false,
  linkInsertButtons: ['linkBack'],
  linkEditButtons: ['linkOpen', 'linkEdit', 'linkRemove'],
  toolbarButtons: ['bold', 'italic', 'insertLink', 'embedly'],
  charCounterCount: false,
  dragInline: false,
  plugins: ['draggable', 'image'],
  theme: 'dark',

  // image params
  imageEditButtons: ['imageCaption', 'imageLink', 'linkOpen', 'linkEdit', 'linkRemove', 'imageRemove'],
  imageResize: false,
  // Unconstrained image width in the editor.  Default is 300 pixels.
  imageDefaultWidth: 0,

  // image upload params
  imageUploadParam: 'file',
  imageUploadMethod: 'POST',
  // allow 100MB uploads per struts default config
  imageMaxSize: 104857600,

};

const desktopEditorConfig = {
  ...sharedEditorConfig,
  toolbarInline: true,
  heightMin: 550,
  plugins: [...sharedEditorConfig.plugins, 'quickInsert'],
  quickInsertButtons: ['image', 'embedly', 'hr'],
  toolbarButtons: [...sharedEditorConfig.toolbarButtons, '|', 'h1', 'h2', '|', 'formatOL', 'formatUL', '|', 'quote']
};

const mobileEditorConfig = {
  ...sharedEditorConfig,
  heightMin: 150,
  plugins: [...sharedEditorConfig.plugins, 'image', 'link', 'lists', 'paragraphFormat'],
  // this property (listing enabled plugins) is the only way to disable the quick insert button for our mobile editor
  pluginsEnabled: ['draggable', 'image', 'link', 'lists', 'paragraphFormat', 'quote'],
  toolbarBottom: true,
  toolbarButtons: [
    ...sharedEditorConfig.toolbarButtons,
    '|',
    'paragraphFormat',
    'formatOL',
    'formatUL',
    '|',
    'insertHR',
    'quote',
    '|',
    'insertImage',
  ],
  paragraphFormat: { N: 'Normal', H1: 'Heading 1', H2: 'Heading 2' },
  imageInsertButtons: ['imageBack', '|', 'imageUpload']
};

interface State {
  model: string;
}

interface ParentProps {
  initialValue?: string | null;
  onChange: (model: string) => void;
  postOid: string;
  onImageUpload: (isLoading: boolean, isError?: boolean) => void;
}

type Props =
  ParentProps &
  InjectedIntlProps;

class EditorComponent extends React.Component<Props, State> {
  constructor (props: Props) {
    super(props);

    this.state = {
      model: this.props.initialValue || ''
    };
  }

  // zb: ignore all attempts to re-render the component due to state change
  // this way we aren't constantly re-rendering the editor contents and
  // causing the embedly cards to rebuild with each keystroke
  shouldComponentUpdate(_nextProps: Props, _nextState: State) {
    return false;
  }

  componentWillReceiveProps(nextProps: Props) {
    if (nextProps.initialValue !== this.props.initialValue) {
      this.setState(ss => ({ model: nextProps.initialValue || ss.model }));
    }
    currentModel = this.state.model;
  }

  handleModelChange = (model: string) => {
    this.setState({
      model
    });

    this.props.onChange(model);
  }

  render () {
    const { intl, postOid, onImageUpload } = this.props;
    const placeholderText = intl.formatMessage(PostMessages.PostBodyPlaceholder);

    const sharedProps = {
      tag: 'textarea',
      onModelChange: this.handleModelChange,
      config: {
        imageUploadURL: '/api/posts/' + postOid + '/attachments',
        placeholderText,
        events: {
          // tslint:disable no-any
          'froalaEditor.image.beforeUpload': async (_: any) => {
            onImageUpload(true);
          },
          'froalaEditor.image.inserted': async (_e: any, _editor: any, _$img: any, _response: any) => {
            onImageUpload(false);
            const $img = $(_$img);
            // bl: the parent is a paragraph, which shouldn't have block elements in it, so let's remove
            // that wrapping paragraph and just replace it with the wrapped image
            $img.parent().replaceWith($img);
            $img.wrap('<div class="imgWrapper"></div>');
            const $imgWrapper = $img.parent();
            // bl: the new parent should be the imgWrapper, so just insert an empty paragraph after it
            // bl: only do this if the image is the last child. otherwise, this isn't really necessary
            if ($imgWrapper.is(':last-child')) {
              $imgWrapper.after('<p><br/></p>');
            }
          },
          'froalaEditor.image.beforeRemove': async (_e: any, _editor: any, _$img: any, _response: any) => {
            $(_$img).closest('.imgWrapper').remove();
          },
          'froalaEditor.image.error': async () => {
            onImageUpload(false, true);
          }
          // tslint:enable no-any
        }
      }
    };

    const desktopInitProps = {
      config: {
        events: {
          // tslint:disable no-any
          'froalaEditor.initialized': async  (_e: any, editor: any) => {
            desktopEditor = editor;
            currentModel = this.state.model;
          }
          // tslint:enable no-any
        }
      }
    };

    // tslint:disable-next-line no-any
    const finalDesktopProps: any = DeepMerge.all([sharedProps, desktopInitProps]);

    const mobileInitProps = {
      config: {
        events: {
          // tslint:disable no-any
          'froalaEditor.initialized': function (_e: any, editor: any) {
            mobileEditor = editor;
          }
          // tslint:enable no-any
        }
      }
    };

    // tslint:disable-next-line no-any
    const finalMobileProps: any = DeepMerge.all([sharedProps, mobileInitProps]);

    return (
      <React.Fragment>
        {FroalaEmbedlyStyle}
        <DesktopEditorWrapper>
          <FroalaEditor
            {...sharedProps}
            config={{
              ...desktopEditorConfig,
              ...finalDesktopProps.config,
            }}
          />
        </DesktopEditorWrapper>

        <MobileEditorWrapper>
          <FroalaEditor
            {...sharedProps}
            config={{
              ...mobileEditorConfig,
              ...finalMobileProps.config
            }}
          />
        </MobileEditorWrapper>
      </React.Fragment>
    );
  }
}

// zb: We will subscribe to media query changes to determine which
// editor we should show (mobile or desktop)
const smallMediaQuery = '(max-width: ' + themeScreenSize.xs + 'px)';

// tslint:disable no-any
function MediaQueryChange(mq: any) {
  if (mq.matches) {
    // mobile size
    mobileEditor.html.set(currentModel);
  } else {
    // desktop size
    desktopEditor.html.set(currentModel);
  }
}

export const Editor = compose(
  lifecycle<Props, State>({

    // tslint:disable object-literal-shorthand
    componentDidMount: async function () {

      const mq = window.matchMedia(smallMediaQuery);
      if (mq.addEventListener) {
        mq.addEventListener('change', MediaQueryChange);
      } else { // zb: adding this as a fallback for safari even though it is now deprecated
        mq.addListener(MediaQueryChange);
      }

      MediaQueryChange(mq);
    },
    // tslint:enable object-literal-shorthand
  }),
  injectIntl
)(EditorComponent) as React.ComponentClass<ParentProps>;
