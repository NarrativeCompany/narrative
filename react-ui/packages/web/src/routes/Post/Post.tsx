import * as React from 'react';
import { generatePath, RouteComponentProps, withRouter } from 'react-router';
import { Form, FormikProps, FormikState, withFormik } from 'formik';
import { branch, compose, lifecycle, renderComponent, withHandlers, withProps } from 'recompose';
import { Layout, message, Modal } from 'antd';
import { FormattedMessage, injectIntl, InjectedIntlProps } from 'react-intl';
import { ViewWrapper, ViewWrapperProps } from '../../shared/components/ViewWrapper';
import { FlexContainer } from '../../shared/styled/shared/containers';
import { SEO } from '../../shared/components/SEO';
import { Header } from '../../layout/Header/Header';
import { Button } from '../../shared/components/Button';
import { Link } from '../../shared/components/Link';
import { NotFound } from '../../shared/components/NotFound';
import { Loading } from '../../shared/components/Loading';
import { ErrorModal } from '../../shared/components/ErrorModal';
import { DeletePostConfirmation } from './components/DeletePostConfirmation';
import { ExitPostConfirmation } from './components/ExitPostConfirmation';
import { Carousel, carouselRef } from '../../shared/components/Carousel';
import { Paragraph, ParagraphProps } from '../../shared/components/Paragraph';
import { CreatePost } from './components/CreatePost';
import { SelectChannels } from './components/SelectChannels';
import { PostConfirmation } from './components/PostConfirmation';
import { PostMessages } from '../../shared/i18n/PostMessages';
import { SharedComponentMessages } from '../../shared/i18n/SharedComponentMessages';
import { WebRoute } from '../../shared/constants/routes';
import { SEOMessages } from '../../shared/i18n/SEOMessages';
import {
  CarouselState,
  initialCarouselState,
  withCarouselController,
  WithCarouselControllerProps
} from '../../shared/containers/withCarouselController';
import {
  applyExceptionToState,
  EditPostDetail,
  initialFormState,
  MethodError,
  Niche,
  User,
  PublicationDetail,
  postFormUtil,
  PostInput,
  PostTextInput,
  withDeletePost,
  WithDeletePostProps,
  withEditPost,
  WithEditPostProps,
  withPostForEdit,
  WithPostForEditProps,
  withState,
  WithStateProps,
  withSubmitPost,
  WithSubmitPostProps,
  withValidatePost,
  WithValidatePostProps,
  handleFormlessServerOperation
} from '@narrative/shared';
import {
  withExtractedCurrentUser,
  WithExtractedCurrentUserProps
} from '../../shared/containers/withExtractedCurrentUser';
import styled from '../../shared/styled';
import { NicheLink } from '../../shared/components/niche/NicheLink';

// variable declarations for the save to draft controls and unload event
// tslint:disable no-any
let saveDraftInterval: any;
let lastSavedFormValues: string;
let currentFormValues: string;
let beforeUnloadHandlerRef: any;
// tslint:enable no-any

const EditorLayout = styled(Layout)`
  background: #fff;
`;

export const StyledViewWrapper = styled<ViewWrapperProps>(ViewWrapper)`
  width: 100%;
  margin-top: 64px;
`;

const HeaderTitle = styled<ParagraphProps>(Paragraph)`
  @media screen and (max-width: 767px) {
    margin-left: 30px;
  }
`;

type ConfirmationModalType = 'delete' | 'exit';

interface InitialStateChannelProps {
  selectedNiches: Niche[];
  selectedPublicationDetail?: PublicationDetail;
}

type State =
  CarouselState &
  MethodError &
  InitialStateChannelProps & {
  isValidatingPostText: boolean;
  isDeletePostModalVisible: boolean;
  isErrorModalVisible: boolean;
  isExitModalVisible: boolean;
  postPrettyUrlString: string;
  postLive: boolean;
  edit?: boolean;
  pendingPublicationApproval: boolean;
  blockedByNiche?: Niche;
};
const initialState: State = Object.assign(initialCarouselState, {
  isValidatingPostText: false,
  isDeletePostModalVisible: false,
  isErrorModalVisible: false,
  isExitModalVisible: false,
  selectedNiches: [],
  postPrettyUrlString: '',
  postLive: false,
  pendingPublicationApproval: false,
  ...initialFormState
});

interface WithProps {
  postOid?: string;
  currentUser: User;
}

interface WithHandlers {
  handleStepOneValidation: () => void;
  handlePrevClickFromStepTwo: () => void;
  handleExitPost: () => void;
  handleAddSelectedNiche: (niche: Niche) => void;
  handleRemoveSelectedNiche: (niche: Niche) => void;
  handleEditorChange: (model: string) => void;
  handleImageUpload: (isLoading: boolean, isError?: boolean) => void;
  handleDeletePost: () => void;
  handleShowConfirmationModal: (modalType: ConfirmationModalType) => void;
  handleDismissConfirmationModal: (modalType: ConfirmationModalType) => void;
  acknowledgeBlockedByNiche: () => void;
}

type WithFormikProps =
  WithCarouselControllerProps &
  WithStateProps<State> &
  WithSubmitPostProps &
  WithEditPostProps &
  FormikProps<PostInput> &
  WithPostForEditProps &
  WithProps;

type WithLifeCycleProps =
  FormikProps<PostInput> &
  WithSubmitPostProps &
  WithEditPostProps &
  WithExtractedCurrentUserProps &
  WithStateProps<State> &
  RouteComponentProps<{}> &
  InjectedIntlProps &
  WithPostForEditProps &
  WithProps;

type WithHandlersProps =
  WithStateProps<State> &
  RouteComponentProps<{}> &
  FormikProps<PostInput> &
  WithCarouselControllerProps &
  InjectedIntlProps &
  WithValidatePostProps &
  WithSubmitPostProps &
  WithEditPostProps &
  WithDeletePostProps &
  WithPostForEditProps &
  WithProps;

type WithCreateOrUpdatePostProps =
  WithSubmitPostProps &
  WithEditPostProps &
  WithProps &
  FormikProps<PostInput>;

type Props =
  WithStateProps<State> &
  WithCarouselControllerProps &
  WithHandlers &
  FormikProps<PostInput> &
  WithPostForEditProps &
  WithEditPostProps &
  WithProps &
  InjectedIntlProps;

const Post: React.SFC<Props> = (props) => {
  const {
    handleNextSlideClick,
    handleStepOneValidation,
    handlePrevClickFromStepTwo,
    handleExitPost,
    handleDeletePost,
    handleEditorChange,
    handleImageUpload,
    handleAddSelectedNiche,
    handleRemoveSelectedNiche,
    handleShowConfirmationModal,
    handleDismissConfirmationModal,
    acknowledgeBlockedByNiche,
    postForEditLoading,
    availablePublications,
    state,
    values,
    errors,
    isSubmitting,
    postOid,
    setFieldValue,
    submitPostInitialValues,
    authorPersonalJournalOid,
    currentUser
  } = props;

  if (!postOid) {
    return <Loading/>;
  }

  const editPostDetail = props.editPostDetail as EditPostDetail;

  let publishToPrimaryChannel: string | undefined;

  if (submitPostInitialValues && submitPostInitialValues.publishToPrimaryChannel) {
    publishToPrimaryChannel = submitPostInitialValues.publishToPrimaryChannel;
  }

  const exitBtnText = values.draft ? PostMessages.SaveAndExitBtnText : PostMessages.DiscardAndExitBtnText;

  const headerTitle = (
    <HeaderTitle style={{ fontWeight: 600 }}>
      <FormattedMessage {...(values.draft ? PostMessages.NavBarTitle : PostMessages.NavBarTitleEdit)}/>
    </HeaderTitle>
  );

  const formControls = state.currentSlide !== 2 && (
    <FlexContainer justifyContent="flex-end" alignItems="center">
      <Link.Anchor
        color="light"
        size="small"
        style={{ textTransform: 'uppercase', marginRight: 25 }}
        onClick={() => handleShowConfirmationModal('delete')}
      >
        <FormattedMessage {...SharedComponentMessages.DeleteBtnText}/>
      </Link.Anchor>

      <Button
        noShadow={true}
        onClick={() => handleShowConfirmationModal('exit')}
      >
        <FormattedMessage {...exitBtnText}/>
      </Button>
    </FlexContainer>
  );

  return (
    <EditorLayout>
      <SEO
        title={SEOMessages.PostTitle}
        description={SEOMessages.PostDescription}
      />

      <Header
        headerLeftContent={headerTitle}
        headerRightContent={!postForEditLoading && formControls}
        position="fixed"
        logoIsLink={false}
      />

      <DeletePostConfirmation
        visible={state.isDeletePostModalVisible}
        dismiss={() => handleDismissConfirmationModal('delete')}
        onDeletePost={handleDeletePost}
      />

      <ExitPostConfirmation
        isDraft={values.draft}
        visible={state.isExitModalVisible}
        dismiss={() => handleDismissConfirmationModal('exit')}
        onExitClick={handleExitPost}
      />

      <ErrorModal
        visible={state.isErrorModalVisible}
        dismiss={() => props.setState(ss => ({ ...ss, isErrorModalVisible: false }))}
        title={<FormattedMessage {...PostMessages.UploadImageErrorTitle}/>}
        description={<FormattedMessage {...PostMessages.UploadImageErrorMessage}/>}
        gifType="robot"
        btnText={<FormattedMessage {...SharedComponentMessages.CloseBtnText}/>}
      />

      <Modal
        visible={!!state.blockedByNiche}
        onCancel={acknowledgeBlockedByNiche}
        footer={null}
      >
        {state.blockedByNiche &&
          <FormattedMessage
            {...PostMessages.AlreadyBlockedByNiche}
            values={{nicheLink: <NicheLink niche={state.blockedByNiche} target="_blank"/>}}
          />
        }
      </Modal>

      <Form>
        <Carousel ref={carouselRef}>
          <CreatePost
            onNextClick={handleStepOneValidation}
            onEditorChange={model => handleEditorChange(model)}
            onImageUpload={handleImageUpload}
            formValues={values}
            bodyError={errors.body as string}
            postOid={postOid}
            isValidating={state.isValidatingPostText}
            setFieldValue={setFieldValue}
            postLive={editPostDetail.postDetail.post.postLive}
          />

          <SelectChannels
            onPrevClick={handlePrevClickFromStepTwo}
            onNextClick={handleNextSlideClick}
            onAddSelectedNiche={niche => handleAddSelectedNiche(niche)}
            onRemoveSelectedNiche={niche => handleRemoveSelectedNiche(niche)}
            selectedNiches={state.selectedNiches}
            publishToPublication={state.selectedPublicationDetail && state.selectedPublicationDetail.publication}
            authorPersonalJournalOid={authorPersonalJournalOid}
            availablePublications={availablePublications}
            currentSlide={state.currentSlide}
            isSubmitting={isSubmitting}
            methodError={state.methodError}
            formValues={values}
          />

          <PostConfirmation
            currentSlide={state.currentSlide}
            currentUser={currentUser}
            postDetail={editPostDetail.postDetail}
            selectedNiches={state.selectedNiches}
            selectedPublicationDetail={state.selectedPublicationDetail}
            publishToPrimaryChannel={publishToPrimaryChannel}
            authorPersonalJournalOid={authorPersonalJournalOid}
            postPrettyUrlString={state.postPrettyUrlString}
            postLive={state.postLive}
            edit={state.edit}
            pendingPublicationApproval={state.pendingPublicationApproval}
            postOid={postOid}
          />
        </Carousel>
      </Form>
    </EditorLayout>
  );
};

async function createPost (props: WithCreateOrUpdatePostProps, formValues: PostInput): Promise<EditPostDetail> {
  const { submitPost } = props;

  return await submitPost(formValues);
}

async function updatePost (
  props: WithCreateOrUpdatePostProps,
  formValues: PostInput,
  isDraft?: boolean
): Promise<EditPostDetail | null> {
  const { editPost, postOid } = props;

  if (!postOid) {
    return new Promise<null>(() => null);
  }

  const input: PostInput = {
    ...formValues,
    draft: isDraft !== undefined ? isDraft : true
  };

  return await editPost(input, postOid);
}

function setAutoSaveTimerIfDraft (props: WithLifeCycleProps | WithHandlersProps) {
  // don't ever set a timer if this is a live post
  if (!props.values.draft) {
    return;
  }
  setAutoSaveTimer(props);
}

function setAutoSaveTimer (props: WithLifeCycleProps | WithHandlersProps) {
  const { setErrors, setState, intl: { formatMessage } } = props;

  // just to be safe, clear any potentially existing timer
  clearAutoSaveTimer();

  saveDraftInterval = setInterval(async () => {
    // short circuit auto save for any of the above reasons
    if (!currentFormValues || !lastSavedFormValues) {
      return;
    }

    // diff the initial serialized values to updated serialized form values
    const diffFormValues = currentFormValues === lastSavedFormValues;

    // if the values don't match, save the draft, update the url to edit, and alert the user
    if (!diffFormValues) {
      clearAutoSaveTimer();

      try {
        const parsedInput = JSON.parse(currentFormValues);
        await updatePost(props, parsedInput);

        // set last save form values to current values at time of save to draft
        lastSavedFormValues = currentFormValues;

        message.success(formatMessage(PostMessages.SavedToDrafts));
      } catch (err) {
        applyExceptionToState(err, setErrors, setState);
      } finally {
        setAutoSaveTimer(props);
      }
    }
  }, 10000);
}

function clearAutoSaveTimer () {
  if (!saveDraftInterval) {
    return;
  }
  clearInterval(saveDraftInterval);
  saveDraftInterval = null;
}

function setSelectedChannelsInitialState(props: WithLifeCycleProps) {
  const { selectedPublicationDetail, initialSelectedNiches, setState } = props;

  const newState: InitialStateChannelProps = {
    selectedNiches: initialSelectedNiches,
    selectedPublicationDetail
  };

  setState(ss => ({...ss, ...newState}));
}

async function saveDraftOnInitialRender (props: WithLifeCycleProps) {
  const { postOid, history } = props;

  // jw: if we already have a postOid there is no need to create one and redirect.
  if (postOid) {
    return;
  }

  const editPostDetail = await handleFormlessServerOperation(() => createPost(props, {
    publishToPrimaryChannel: undefined,
    ageRestricted: false,
    disableComments: false,
    draft: true,
    publishToNiches: []
  }));

  // jw: if we get back post details then let's redirect to the edit URL for the new post.
  if (editPostDetail) {
    const postOidFromResponse =
      editPostDetail &&
      editPostDetail.postDetail &&
      editPostDetail.postDetail.post &&
      editPostDetail.postDetail.post.oid;

    history.push(generatePath(WebRoute.Post, { postOid: postOidFromResponse }));
    return;
  }

  // jw: otherwise, there was some kind of error server side preventing the user from creating this post (likely a
  //     activity rate limit that the user has hit. Take the user back and trust the error boundary to render the error
  history.goBack();
}

function resetAutoSaveVariablesOnUnmount () {
  clearAutoSaveTimer();
  saveDraftInterval = undefined;
  lastSavedFormValues = '';
  currentFormValues = '';
}

function handleShowOrHideConfirmationModal (
  props: WithHandlersProps,
  modalType: ConfirmationModalType,
  isVisible: boolean
) {
  const { setState } = props;

  let modalStatePropName: string;

  if (modalType === 'delete') {
    modalStatePropName = 'isDeletePostModalVisible';
  } else {
    modalStatePropName = 'isExitModalVisible';
  }

  setState(ss => ({ ...ss, [modalStatePropName]: isVisible }));
}

// tslint:disable-next-line no-any
async function beforeUnloadEventHandler (this: any, e: BeforeUnloadEvent) {
  const unloadMessage = this && this.unloadMessage;
  const hasFormChanged = currentFormValues !== lastSavedFormValues;

  if (hasFormChanged) {
    if (e) {
      e.returnValue = unloadMessage;
    }
    return unloadMessage;
  }
}

export default compose(
  withRouter,
  withExtractedCurrentUser,
  withProps((props: WithExtractedCurrentUserProps & RouteComponentProps<{postOid?: string}>) => {
    const { match: { params } } = props;

    return { postOid: params.postOid };
  }),
  withPostForEdit,
  // short circuit while we're loading edit post details to prevent formik from setting incorrect initial form values
  branch((props: WithPostForEditProps) => props.postForEditLoading,
    renderComponent(() => <Loading/>)
  ),
  branch((props: WithPostForEditProps) => props.postNotFound,
    renderComponent(() => <NotFound/>)
  ),
  injectIntl,
  withState<State>(initialState),
  withCarouselController,
  withValidatePost,
  withSubmitPost,
  withEditPost,
  withDeletePost,
  withFormik<WithFormikProps, PostInput>({
    ...postFormUtil,
    mapPropsToValues: (props) => postFormUtil.mapPropsToValues(props.submitPostInitialValues),
    handleSubmit: async (values, { props, setErrors, setSubmitting }) => {
      const { setState, isSubmitting, handleNextSlideClick } = props;

      if (isSubmitting) {
        return;
      }

      // clear autosave interval, methodError, destroy antd message,
      // and remove beforeunload event since we're now attempting to publish
      clearAutoSaveTimer();
      message.destroy();
      removeEventListener('beforeunload', beforeUnloadHandlerRef);
      setState(ss => ({ ...ss, methodError: null }));

      try {
        // create input and set draft field to false as we are publishing the post at this point
        const editPostDetail = await updatePost(props, values, false);
        if (editPostDetail) {
          setState(ss => ({
            ...ss,
            postPrettyUrlString: editPostDetail.postDetail.post.prettyUrlString,
            postLive: editPostDetail.postDetail.post.postLive,
            edit: editPostDetail.edit,
            pendingPublicationApproval: !!editPostDetail.postDetail.pendingPublicationApproval,
            selectedNiches: editPostDetail.postDetail.post.publishedToNiches,
            selectedPublicationDetail: editPostDetail.publishedToPublicationDetail
          }));
        }

        handleNextSlideClick();
      } catch (err) {
        applyExceptionToState(err, setErrors, setState);

      } finally {
        setSubmitting(false);
      }
    }
  }),
  lifecycle<WithLifeCycleProps, {}>({
    // tslint:disable object-literal-shorthand
    componentWillMount: async function () {
      // Before the component is rendered, let's save the draft and route to post edit
      await saveDraftOnInitialRender(this.props);
    },
    componentDidMount: async function () {
      const { intl: { formatMessage } } = this.props;
      const unloadMessage = formatMessage(PostMessages.BeforeUnloadReturnMessage);
      window.addEventListener(
        'beforeunload',
        beforeUnloadHandlerRef = beforeUnloadEventHandler.bind({ unloadMessage })
      );

      // if there are initial selected channels and this is the first render, set the selected niches/publication state
      setSelectedChannelsInitialState(this.props);

      lastSavedFormValues = JSON.stringify(this.props.values);
      currentFormValues = lastSavedFormValues;

      // serialize the form values as soon as the component mounts and start setInterval for auto save
      setAutoSaveTimerIfDraft(this.props);
    },
    componentDidUpdate: function () {
      const { values } = this.props;

      // update serialized form values when the component updates
      currentFormValues = JSON.stringify(values);
    },
    componentWillUnmount: function () {
      // clear out any global variables and setInterval when the component is destroyed
      resetAutoSaveVariablesOnUnmount();

      // remove the beforeunload event listener when the component is destroyed
      removeEventListener('beforeunload', beforeUnloadHandlerRef);
    }
    // tslint:enable object-literal-shorthand
  }),
  withHandlers<WithHandlersProps, {}>({
    handleStepOneValidation: (props) => async () => {
      const { values, handleNextSlideClick, validatePost, setState, setErrors, setTouched } = props;

      // set is validating state to true (control for loading prop on next button)
      setState(ss => ({ ...ss, isValidatingPostText: true }));

      // we need to set the form fields on this page to touched in order to field errors
      setTouched({
        title: true,
        subTitle: true,
        body: true,
        canonicalUrl: true
      });

      try {
        // validate the post and move to next step if valid
        const input: PostTextInput = {
          title: values.title,
          subTitle: values.subTitle,
          body: values.body,
          canonicalUrl: values.canonicalUrl
        };
        await validatePost(input);
        handleNextSlideClick();
      } catch (err) {
        applyExceptionToState(err, setErrors, setState);
      }

      // set is validating back to false after async op is complete
      setState(ss => ({ ...ss, isValidatingPostText: false }));
    },
    handlePrevClickFromStepTwo: (props) => () => {
      const { handlePrevSlideClick, setState } = props;

      setState(ss => ({ ...ss, methodError: null }));
      handlePrevSlideClick();
    },
    handleEditorChange: (props) => (model: string) => {
      const { setFieldValue } = props;

      setFieldValue('body', model);
    },
    handleImageUpload: (props) => (isLoading: boolean, isError?: boolean) => {
      const { setState } = props;

      // if we're uploading an image we need to pause the autosave until we have a response from the server
      isLoading ? clearAutoSaveTimer() : setAutoSaveTimerIfDraft(props);

      if (isError) {
        setState(ss => ({ ...ss, isErrorModalVisible: true }));
      }
    },
    handleAddSelectedNiche: (props) => (niche: Niche) => {
      const { setFormikState, setState, blockedInNicheOids } = props;

      // jw: if the niche has already been blocked, then give a warning to that effect.
      if (blockedInNicheOids.indexOf(niche.oid) >= 0) {
        props.setState(ss => ({ ...ss, blockedByNiche: niche }));

        return;
      }

      setState(ss => ({
        ...ss,
        selectedNiches: [ ...ss.selectedNiches, niche ],
        methodError: null
      }));

      // tslint:disable-next-line no-any
      setFormikState((prevState: FormikState<any>) => ({
        ...prevState,
        values: {
          ...prevState.values,
          publishToNiches: [
            ...prevState.values.publishToNiches,
            niche.oid
          ]
        }
      }));
    },
    handleRemoveSelectedNiche: (props) => (niche: Niche) => {
      const { setFormikState, setState, state } = props;

      const selectedNiches = state.selectedNiches.filter(n => n.oid !== niche.oid);
      const selectedNicheOids = selectedNiches.map(n => n.oid);

      setState(ss => ({ ...ss, selectedNiches }));

      // tslint:disable-next-line no-any
      setFormikState((prevState: FormikState<any>) => ({
        ...prevState,
        values: {
          ...prevState.values,
          publishToNiches: selectedNicheOids
        }
      }));
    },
    handleShowConfirmationModal: (props) => (modalType: ConfirmationModalType) => {
      // clear the autosave interval to prevent posting while modal is open
      clearAutoSaveTimer();
      handleShowOrHideConfirmationModal(props, modalType, true);
    },
    handleDismissConfirmationModal: (props) => async (modalType: ConfirmationModalType) => {
      // start the autosave interval if the user dismissed/cancelled closing modal
      setAutoSaveTimerIfDraft(props);
      handleShowOrHideConfirmationModal(props, modalType, false);
    },
    handleExitPost: (props) => async () => {
      const { history, values } = props;

      // bl: only save on exit if this is a draft. if it's a live post, the button is to discard and exit,
      // so do not save in that case.
      if (values.draft) {
        await updatePost(props, values);
      }

      history.push(values.draft ? WebRoute.MemberManageDraftsPosts : WebRoute.MemberManagePublishedPosts);
    },
    handleDeletePost: (props) => async () => {
      const { deletePost, setErrors, setState, postOid, history, values } = props;

      if (postOid) {
        try {
          await deletePost(postOid);
        } catch (err) {
          applyExceptionToState(err, setErrors, setState);
        }
      }

      history.push(values.draft ? WebRoute.MemberManageDraftsPosts : WebRoute.MemberManagePublishedPosts);
    },
    acknowledgeBlockedByNiche: (props) => () => {
      props.setState(ss => ({ ...ss, blockedByNiche: undefined }));
    }
  })
)(Post) as React.ComponentClass<{}>;
