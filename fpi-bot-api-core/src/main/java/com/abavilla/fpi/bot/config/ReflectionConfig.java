/******************************************************************************
 * FPI Application - Abavilla                                                 *
 * Copyright (C) 2022  Vince Jerald Villamora                                 *
 *                                                                            *
 * This program is free software: you can redistribute it and/or modify       *
 * it under the terms of the GNU General Public License as published by       *
 * the Free Software Foundation, either version 3 of the License, or          *
 * (at your option) any later version.                                        *
 *                                                                            *
 * This program is distributed in the hope that it will be useful,            *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the              *
 * GNU General Public License for more details.                               *
 *                                                                            *
 * You should have received a copy of the GNU General Public License          *
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.     *
 ******************************************************************************/

package com.abavilla.fpi.bot.config;

import com.abavilla.fpi.fw.config.BaseReflectionConfig;
import com.abavilla.fpi.meta.ext.dto.MetaHookEvtDto;
import com.abavilla.fpi.meta.ext.dto.ProfileReqReply;
import com.abavilla.fpi.meta.ext.dto.msgr.EntryDto;
import com.abavilla.fpi.meta.ext.dto.msgr.MessagingDto;
import com.abavilla.fpi.meta.ext.dto.msgr.MsgAttchmtDto;
import com.abavilla.fpi.meta.ext.dto.msgr.MsgDtlDto;
import com.abavilla.fpi.meta.ext.dto.msgr.MsgrReqReply;
import com.abavilla.fpi.meta.ext.dto.msgr.ProfileDto;
import com.abavilla.fpi.meta.ext.dto.msgr.QuickReplyDto;
import com.abavilla.fpi.meta.ext.dto.msgr.ReferralDto;
import com.abavilla.fpi.meta.ext.dto.msgr.ext.MetaMsgEvtAttchmtDto;
import com.abavilla.fpi.meta.ext.dto.msgr.ext.MetaMsgEvtDto;
import com.pengrad.telegrambot.AttachName;
import com.pengrad.telegrambot.model.Animation;
import com.pengrad.telegrambot.model.Audio;
import com.pengrad.telegrambot.model.BotCommand;
import com.pengrad.telegrambot.model.CallbackQuery;
import com.pengrad.telegrambot.model.Chat;
import com.pengrad.telegrambot.model.ChatAdministratorRights;
import com.pengrad.telegrambot.model.ChatInviteLink;
import com.pengrad.telegrambot.model.ChatJoinRequest;
import com.pengrad.telegrambot.model.ChatLocation;
import com.pengrad.telegrambot.model.ChatMember;
import com.pengrad.telegrambot.model.ChatMemberUpdated;
import com.pengrad.telegrambot.model.ChatPermissions;
import com.pengrad.telegrambot.model.ChatPhoto;
import com.pengrad.telegrambot.model.ChosenInlineResult;
import com.pengrad.telegrambot.model.Contact;
import com.pengrad.telegrambot.model.DeleteMyCommands;
import com.pengrad.telegrambot.model.Dice;
import com.pengrad.telegrambot.model.Document;
import com.pengrad.telegrambot.model.File;
import com.pengrad.telegrambot.model.Game;
import com.pengrad.telegrambot.model.GameHighScore;
import com.pengrad.telegrambot.model.InlineQuery;
import com.pengrad.telegrambot.model.Invoice;
import com.pengrad.telegrambot.model.Location;
import com.pengrad.telegrambot.model.MaskPosition;
import com.pengrad.telegrambot.model.MenuButton;
import com.pengrad.telegrambot.model.MenuButtonCommands;
import com.pengrad.telegrambot.model.MenuButtonDefault;
import com.pengrad.telegrambot.model.MenuButtonWebApp;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.MessageAutoDeleteTimerChanged;
import com.pengrad.telegrambot.model.MessageEntity;
import com.pengrad.telegrambot.model.MessageId;
import com.pengrad.telegrambot.model.OrderInfo;
import com.pengrad.telegrambot.model.PhotoSize;
import com.pengrad.telegrambot.model.Poll;
import com.pengrad.telegrambot.model.PollAnswer;
import com.pengrad.telegrambot.model.PollOption;
import com.pengrad.telegrambot.model.PreCheckoutQuery;
import com.pengrad.telegrambot.model.ProximityAlertTriggered;
import com.pengrad.telegrambot.model.ResponseParameters;
import com.pengrad.telegrambot.model.SentWebAppMessage;
import com.pengrad.telegrambot.model.ShippingAddress;
import com.pengrad.telegrambot.model.ShippingQuery;
import com.pengrad.telegrambot.model.Sticker;
import com.pengrad.telegrambot.model.StickerSet;
import com.pengrad.telegrambot.model.SuccessfulPayment;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.User;
import com.pengrad.telegrambot.model.UserProfilePhotos;
import com.pengrad.telegrambot.model.Venue;
import com.pengrad.telegrambot.model.Video;
import com.pengrad.telegrambot.model.VideoChatEnded;
import com.pengrad.telegrambot.model.VideoChatParticipantsInvited;
import com.pengrad.telegrambot.model.VideoChatScheduled;
import com.pengrad.telegrambot.model.VideoChatStarted;
import com.pengrad.telegrambot.model.VideoNote;
import com.pengrad.telegrambot.model.Voice;
import com.pengrad.telegrambot.model.WebAppData;
import com.pengrad.telegrambot.model.WebAppInfo;
import com.pengrad.telegrambot.model.WebhookInfo;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandScope;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandScopeAllChatAdministrators;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandScopeAllGroupChats;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandScopeAllPrivateChats;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandScopeDefault;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandsScopeChat;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandsScopeChatAdministrators;
import com.pengrad.telegrambot.model.botcommandscope.BotCommandsScopeChatMember;
import com.pengrad.telegrambot.model.request.CallbackGame;
import com.pengrad.telegrambot.model.request.ChatAction;
import com.pengrad.telegrambot.model.request.ForceReply;
import com.pengrad.telegrambot.model.request.InlineKeyboardButton;
import com.pengrad.telegrambot.model.request.InlineKeyboardMarkup;
import com.pengrad.telegrambot.model.request.InlineQueryResult;
import com.pengrad.telegrambot.model.request.InlineQueryResultArticle;
import com.pengrad.telegrambot.model.request.InlineQueryResultAudio;
import com.pengrad.telegrambot.model.request.InlineQueryResultCachedAudio;
import com.pengrad.telegrambot.model.request.InlineQueryResultCachedDocument;
import com.pengrad.telegrambot.model.request.InlineQueryResultCachedGif;
import com.pengrad.telegrambot.model.request.InlineQueryResultCachedMpeg4Gif;
import com.pengrad.telegrambot.model.request.InlineQueryResultCachedPhoto;
import com.pengrad.telegrambot.model.request.InlineQueryResultCachedSticker;
import com.pengrad.telegrambot.model.request.InlineQueryResultCachedVideo;
import com.pengrad.telegrambot.model.request.InlineQueryResultCachedVoice;
import com.pengrad.telegrambot.model.request.InlineQueryResultContact;
import com.pengrad.telegrambot.model.request.InlineQueryResultDocument;
import com.pengrad.telegrambot.model.request.InlineQueryResultGame;
import com.pengrad.telegrambot.model.request.InlineQueryResultGif;
import com.pengrad.telegrambot.model.request.InlineQueryResultLocation;
import com.pengrad.telegrambot.model.request.InlineQueryResultMpeg4Gif;
import com.pengrad.telegrambot.model.request.InlineQueryResultPhoto;
import com.pengrad.telegrambot.model.request.InlineQueryResultVenue;
import com.pengrad.telegrambot.model.request.InlineQueryResultVideo;
import com.pengrad.telegrambot.model.request.InlineQueryResultVoice;
import com.pengrad.telegrambot.model.request.InputContactMessageContent;
import com.pengrad.telegrambot.model.request.InputInvoiceMessageContent;
import com.pengrad.telegrambot.model.request.InputLocationMessageContent;
import com.pengrad.telegrambot.model.request.InputMedia;
import com.pengrad.telegrambot.model.request.InputMediaAnimation;
import com.pengrad.telegrambot.model.request.InputMediaAudio;
import com.pengrad.telegrambot.model.request.InputMediaDocument;
import com.pengrad.telegrambot.model.request.InputMediaPhoto;
import com.pengrad.telegrambot.model.request.InputMediaVideo;
import com.pengrad.telegrambot.model.request.InputMessageContent;
import com.pengrad.telegrambot.model.request.InputTextMessageContent;
import com.pengrad.telegrambot.model.request.InputVenueMessageContent;
import com.pengrad.telegrambot.model.request.Keyboard;
import com.pengrad.telegrambot.model.request.KeyboardButton;
import com.pengrad.telegrambot.model.request.KeyboardButtonPollType;
import com.pengrad.telegrambot.model.request.LabeledPrice;
import com.pengrad.telegrambot.model.request.LoginUrl;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.model.request.ReplyKeyboardMarkup;
import com.pengrad.telegrambot.model.request.ReplyKeyboardRemove;
import com.pengrad.telegrambot.model.request.ShippingOption;
import com.pengrad.telegrambot.passport.Credentials;
import com.pengrad.telegrambot.passport.DataCredentials;
import com.pengrad.telegrambot.passport.DecryptedData;
import com.pengrad.telegrambot.passport.EncryptedCredentials;
import com.pengrad.telegrambot.passport.EncryptedPassportElement;
import com.pengrad.telegrambot.passport.FileCredentials;
import com.pengrad.telegrambot.passport.IdDocumentData;
import com.pengrad.telegrambot.passport.PassportData;
import com.pengrad.telegrambot.passport.PassportElementError;
import com.pengrad.telegrambot.passport.PassportElementErrorDataField;
import com.pengrad.telegrambot.passport.PassportElementErrorFile;
import com.pengrad.telegrambot.passport.PassportElementErrorFiles;
import com.pengrad.telegrambot.passport.PassportElementErrorFrontSide;
import com.pengrad.telegrambot.passport.PassportElementErrorReverseSide;
import com.pengrad.telegrambot.passport.PassportElementErrorSelfie;
import com.pengrad.telegrambot.passport.PassportElementErrorTranslationFile;
import com.pengrad.telegrambot.passport.PassportElementErrorTranslationFiles;
import com.pengrad.telegrambot.passport.PassportElementErrorUnspecified;
import com.pengrad.telegrambot.passport.PassportFile;
import com.pengrad.telegrambot.passport.PersonalDetails;
import com.pengrad.telegrambot.passport.ResidentialAddress;
import com.pengrad.telegrambot.passport.SecureData;
import com.pengrad.telegrambot.passport.SecureValue;
import com.pengrad.telegrambot.passport.SetPassportDataErrors;
import com.pengrad.telegrambot.request.AbstractMultipartRequest;
import com.pengrad.telegrambot.request.AbstractSendRequest;
import com.pengrad.telegrambot.request.AbstractUploadRequest;
import com.pengrad.telegrambot.request.AddStickerToSet;
import com.pengrad.telegrambot.request.AnswerCallbackQuery;
import com.pengrad.telegrambot.request.AnswerInlineQuery;
import com.pengrad.telegrambot.request.AnswerPreCheckoutQuery;
import com.pengrad.telegrambot.request.AnswerShippingQuery;
import com.pengrad.telegrambot.request.AnswerWebAppQuery;
import com.pengrad.telegrambot.request.ApproveChatJoinRequest;
import com.pengrad.telegrambot.request.BanChatMember;
import com.pengrad.telegrambot.request.BanChatSenderChat;
import com.pengrad.telegrambot.request.BaseRequest;
import com.pengrad.telegrambot.request.Close;
import com.pengrad.telegrambot.request.ContentTypes;
import com.pengrad.telegrambot.request.CopyMessage;
import com.pengrad.telegrambot.request.CreateChatInviteLink;
import com.pengrad.telegrambot.request.CreateInvoiceLink;
import com.pengrad.telegrambot.request.CreateNewStickerSet;
import com.pengrad.telegrambot.request.DeclineChatJoinRequest;
import com.pengrad.telegrambot.request.DeleteChatPhoto;
import com.pengrad.telegrambot.request.DeleteChatStickerSet;
import com.pengrad.telegrambot.request.DeleteMessage;
import com.pengrad.telegrambot.request.DeleteStickerFromSet;
import com.pengrad.telegrambot.request.DeleteWebhook;
import com.pengrad.telegrambot.request.EditChatInviteLink;
import com.pengrad.telegrambot.request.EditMessageCaption;
import com.pengrad.telegrambot.request.EditMessageLiveLocation;
import com.pengrad.telegrambot.request.EditMessageMedia;
import com.pengrad.telegrambot.request.EditMessageReplyMarkup;
import com.pengrad.telegrambot.request.EditMessageText;
import com.pengrad.telegrambot.request.ExportChatInviteLink;
import com.pengrad.telegrambot.request.ForwardMessage;
import com.pengrad.telegrambot.request.GetChat;
import com.pengrad.telegrambot.request.GetChatAdministrators;
import com.pengrad.telegrambot.request.GetChatMember;
import com.pengrad.telegrambot.request.GetChatMemberCount;
import com.pengrad.telegrambot.request.GetChatMenuButton;
import com.pengrad.telegrambot.request.GetCustomEmojiStickers;
import com.pengrad.telegrambot.request.GetFile;
import com.pengrad.telegrambot.request.GetGameHighScores;
import com.pengrad.telegrambot.request.GetMe;
import com.pengrad.telegrambot.request.GetMyCommands;
import com.pengrad.telegrambot.request.GetMyDefaultAdministratorRights;
import com.pengrad.telegrambot.request.GetStickerSet;
import com.pengrad.telegrambot.request.GetUpdates;
import com.pengrad.telegrambot.request.GetUserProfilePhotos;
import com.pengrad.telegrambot.request.GetWebhookInfo;
import com.pengrad.telegrambot.request.LeaveChat;
import com.pengrad.telegrambot.request.LogOut;
import com.pengrad.telegrambot.request.PinChatMessage;
import com.pengrad.telegrambot.request.PromoteChatMember;
import com.pengrad.telegrambot.request.RestrictChatMember;
import com.pengrad.telegrambot.request.RevokeChatInviteLink;
import com.pengrad.telegrambot.request.SendAnimation;
import com.pengrad.telegrambot.request.SendAudio;
import com.pengrad.telegrambot.request.SendChatAction;
import com.pengrad.telegrambot.request.SendContact;
import com.pengrad.telegrambot.request.SendDice;
import com.pengrad.telegrambot.request.SendDocument;
import com.pengrad.telegrambot.request.SendGame;
import com.pengrad.telegrambot.request.SendInvoice;
import com.pengrad.telegrambot.request.SendLocation;
import com.pengrad.telegrambot.request.SendMediaGroup;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.request.SendPhoto;
import com.pengrad.telegrambot.request.SendPoll;
import com.pengrad.telegrambot.request.SendSticker;
import com.pengrad.telegrambot.request.SendVenue;
import com.pengrad.telegrambot.request.SendVideo;
import com.pengrad.telegrambot.request.SendVideoNote;
import com.pengrad.telegrambot.request.SendVoice;
import com.pengrad.telegrambot.request.SetChatAdministratorCustomTitle;
import com.pengrad.telegrambot.request.SetChatDescription;
import com.pengrad.telegrambot.request.SetChatMenuButton;
import com.pengrad.telegrambot.request.SetChatPermissions;
import com.pengrad.telegrambot.request.SetChatPhoto;
import com.pengrad.telegrambot.request.SetChatStickerSet;
import com.pengrad.telegrambot.request.SetChatTitle;
import com.pengrad.telegrambot.request.SetGameScore;
import com.pengrad.telegrambot.request.SetMyCommands;
import com.pengrad.telegrambot.request.SetMyDefaultAdministratorRights;
import com.pengrad.telegrambot.request.SetStickerPositionInSet;
import com.pengrad.telegrambot.request.SetStickerSetThumb;
import com.pengrad.telegrambot.request.SetWebhook;
import com.pengrad.telegrambot.request.StopMessageLiveLocation;
import com.pengrad.telegrambot.request.StopPoll;
import com.pengrad.telegrambot.request.UnbanChatMember;
import com.pengrad.telegrambot.request.UnbanChatSenderChat;
import com.pengrad.telegrambot.request.UnpinAllChatMessages;
import com.pengrad.telegrambot.request.UnpinChatMessage;
import com.pengrad.telegrambot.request.UploadStickerFile;
import com.pengrad.telegrambot.response.BaseResponse;
import com.pengrad.telegrambot.response.ChatInviteLinkResponse;
import com.pengrad.telegrambot.response.GetChatAdministratorsResponse;
import com.pengrad.telegrambot.response.GetChatMemberCountResponse;
import com.pengrad.telegrambot.response.GetChatMemberResponse;
import com.pengrad.telegrambot.response.GetChatMenuButtonResponse;
import com.pengrad.telegrambot.response.GetChatResponse;
import com.pengrad.telegrambot.response.GetCustomEmojiStickersResponse;
import com.pengrad.telegrambot.response.GetFileResponse;
import com.pengrad.telegrambot.response.GetGameHighScoresResponse;
import com.pengrad.telegrambot.response.GetMeResponse;
import com.pengrad.telegrambot.response.GetMyCommandsResponse;
import com.pengrad.telegrambot.response.GetMyDefaultAdministratorRightsResponse;
import com.pengrad.telegrambot.response.GetStickerSetResponse;
import com.pengrad.telegrambot.response.GetUpdatesResponse;
import com.pengrad.telegrambot.response.GetUserProfilePhotosResponse;
import com.pengrad.telegrambot.response.GetWebhookInfoResponse;
import com.pengrad.telegrambot.response.MessageIdResponse;
import com.pengrad.telegrambot.response.MessagesResponse;
import com.pengrad.telegrambot.response.PollResponse;
import com.pengrad.telegrambot.response.SendResponse;
import com.pengrad.telegrambot.response.SentWebAppMessageResponse;
import com.pengrad.telegrambot.response.StringResponse;
import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Classes to register for reflection for Quarkus native image.
 *
 * @author <a href="mailto:vincevillamora@gmail.com">Vince Villamora</a>
 */
@RegisterForReflection(targets = {
  // FB messenger models
  MetaMsgEvtAttchmtDto.class,
  MetaMsgEvtDto.class,
  EntryDto.class,
  MessagingDto.class,
  MsgAttchmtDto.class,
  MsgDtlDto.class,
  MsgrReqReply.class,
  ProfileDto.class,
  QuickReplyDto.class,
  ReferralDto.class,
  MetaHookEvtDto.class,
  ProfileReqReply.class,

  // Telegram Models - botcommandsscope
  BotCommandScope.class,
  BotCommandScopeAllChatAdministrators.class,
  BotCommandScopeAllGroupChats.class,
  BotCommandScopeAllPrivateChats.class,
  BotCommandScopeDefault.class,
  BotCommandsScopeChat.class,
  BotCommandsScopeChatAdministrators.class,
  BotCommandsScopeChatMember.class,

  // Telegram Models - request
  InlineQueryResult.class,
  InputMedia.class,
  InputMessageContent.class,
  Keyboard.class,
  CallbackGame.class,
  ForceReply.class,
  InlineKeyboardButton.class,
  InlineKeyboardMarkup.class,
  InlineQueryResultArticle.class,
  InlineQueryResultAudio.class,
  InlineQueryResultCachedAudio.class,
  InlineQueryResultCachedDocument.class,
  InlineQueryResultCachedGif.class,
  InlineQueryResultCachedMpeg4Gif.class,
  InlineQueryResultCachedPhoto.class,
  InlineQueryResultCachedSticker.class,
  InlineQueryResultCachedVideo.class,
  InlineQueryResultCachedVoice.class,
  InlineQueryResultContact.class,
  InlineQueryResultDocument.class,
  InlineQueryResultGame.class,
  InlineQueryResultGif.class,
  InlineQueryResultLocation.class,
  InlineQueryResultMpeg4Gif.class,
  InlineQueryResultPhoto.class,
  InlineQueryResultVenue.class,
  InlineQueryResultVideo.class,
  InlineQueryResultVoice.class,
  InputContactMessageContent.class,
  InputInvoiceMessageContent.class,
  InputLocationMessageContent.class,
  InputMediaAnimation.class,
  InputMediaAudio.class,
  InputMediaDocument.class,
  InputMediaPhoto.class,
  InputMediaVideo.class,
  InputTextMessageContent.class,
  InputVenueMessageContent.class,
  KeyboardButton.class,
  KeyboardButtonPollType.class,
  LabeledPrice.class,
  LoginUrl.class,
  ReplyKeyboardMarkup.class,
  ReplyKeyboardRemove.class,
  ShippingOption.class,
  ChatAction.class,
  ParseMode.class,

  // Telegram Models - root package
  Animation.class,
  Audio.class,
  BotCommand.class,
  CallbackQuery.class,
  Chat.class,
  Chat.Type.class,
  ChatAdministratorRights.class,
  ChatInviteLink.class,
  ChatJoinRequest.class,
  ChatLocation.class,
  ChatMember.class,
  ChatMember.Status.class,
  ChatMemberUpdated.class,
  ChatPermissions.class,
  ChatPhoto.class,
  ChosenInlineResult.class,
  Contact.class,
  DeleteMyCommands.class,
  Dice.class,
  Document.class,
  File.class,
  Game.class,
  GameHighScore.class,
  InlineQuery.class,
  Invoice.class,
  Location.class,
  MaskPosition.class,
  MaskPosition.Point.class,
  MenuButton.class,
  MenuButtonCommands.class,
  MenuButtonDefault.class,
  MenuButtonWebApp.class,
  Message.class,
  MessageAutoDeleteTimerChanged.class,
  MessageEntity.class,
  MessageEntity.Type.class,
  MessageId.class,
  OrderInfo.class,
  PhotoSize.class,
  Poll.class,
  Poll.Type.class,
  PollAnswer.class,
  PollOption.class,
  PreCheckoutQuery.class,
  ProximityAlertTriggered.class,
  ResponseParameters.class,
  SentWebAppMessage.class,
  ShippingAddress.class,
  ShippingQuery.class,
  Sticker.class,
  Sticker.Type.class,
  StickerSet.class,
  SuccessfulPayment.class,
  Update.class,
  User.class,
  UserProfilePhotos.class,
  Venue.class,
  Video.class,
  VideoChatEnded.class,
  VideoChatParticipantsInvited.class,
  VideoChatScheduled.class,
  VideoChatStarted.class,
  VideoNote.class,
  Voice.class,
  WebAppData.class,
  WebAppInfo.class,
  WebhookInfo.class,

  // Telegram Models - passport
  DecryptedData.class,
  PassportElementError.class,
  Credentials.class,
  DataCredentials.class,
  EncryptedCredentials.class,
  EncryptedPassportElement.class,
  EncryptedPassportElement.Type.class,
  FileCredentials.class,
  IdDocumentData.class,
  PassportData.class,
  PassportElementErrorDataField.class,
  PassportElementErrorFile.class,
  PassportElementErrorFiles.class,
  PassportElementErrorFrontSide.class,
  PassportElementErrorReverseSide.class,
  PassportElementErrorSelfie.class,
  PassportElementErrorTranslationFile.class,
  PassportElementErrorTranslationFiles.class,
  PassportElementErrorUnspecified.class,
  PassportFile.class,
  PersonalDetails.class,
  ResidentialAddress.class,
  SecureData.class,
  SecureValue.class,
  SetPassportDataErrors.class,

  // Telegram Models - request
  AbstractMultipartRequest.class,
  AbstractSendRequest.class,
  AbstractUploadRequest.class,
  BaseRequest.class,
  ContentTypes.class,
  AddStickerToSet.class,
  AnswerCallbackQuery.class,
  AnswerInlineQuery.class,
  AnswerPreCheckoutQuery.class,
  AnswerShippingQuery.class,
  AnswerWebAppQuery.class,
  ApproveChatJoinRequest.class,
  BanChatMember.class,
  BanChatSenderChat.class,
  Close.class,
  CopyMessage.class,
  CreateChatInviteLink.class,
  CreateInvoiceLink.class,
  CreateNewStickerSet.class,
  DeclineChatJoinRequest.class,
  DeleteChatPhoto.class,
  DeleteChatStickerSet.class,
  DeleteMessage.class,
  DeleteStickerFromSet.class,
  DeleteWebhook.class,
  EditChatInviteLink.class,
  EditMessageCaption.class,
  EditMessageLiveLocation.class,
  EditMessageMedia.class,
  EditMessageReplyMarkup.class,
  EditMessageText.class,
  ExportChatInviteLink.class,
  ForwardMessage.class,
  GetChat.class,
  GetChatAdministrators.class,
  GetChatMember.class,
  GetChatMemberCount.class,
  GetChatMenuButton.class,
  GetCustomEmojiStickers.class,
  GetFile.class,
  GetGameHighScores.class,
  GetMe.class,
  GetMyCommands.class,
  GetMyDefaultAdministratorRights.class,
  GetStickerSet.class,
  GetUpdates.class,
  GetUserProfilePhotos.class,
  GetWebhookInfo.class,
  LeaveChat.class,
  LogOut.class,
  PinChatMessage.class,
  PromoteChatMember.class,
  RestrictChatMember.class,
  RevokeChatInviteLink.class,
  SendAnimation.class,
  SendAudio.class,
  SendChatAction.class,
  SendContact.class,
  SendDice.class,
  SendDocument.class,
  SendGame.class,
  SendInvoice.class,
  SendLocation.class,
  SendMediaGroup.class,
  SendMessage.class,
  SendPhoto.class,
  SendPoll.class,
  SendSticker.class,
  SendVenue.class,
  SendVideo.class,
  SendVideoNote.class,
  SendVoice.class,
  SetChatAdministratorCustomTitle.class,
  SetChatDescription.class,
  SetChatMenuButton.class,
  SetChatPermissions.class,
  SetChatPhoto.class,
  SetChatStickerSet.class,
  SetChatTitle.class,
  SetGameScore.class,
  SetMyCommands.class,
  SetMyDefaultAdministratorRights.class,
  SetStickerPositionInSet.class,
  SetStickerSetThumb.class,
  SetWebhook.class,
  StopMessageLiveLocation.class,
  StopPoll.class,
  UnbanChatMember.class,
  UnbanChatSenderChat.class,
  UnpinAllChatMessages.class,
  UnpinChatMessage.class,
  UploadStickerFile.class,

  // Telegram Models - response
  BaseResponse.class,
  ChatInviteLinkResponse.class,
  GetChatAdministratorsResponse.class,
  GetChatMemberCountResponse.class,
  GetChatMemberResponse.class,
  GetChatMenuButtonResponse.class,
  GetChatResponse.class,
  GetCustomEmojiStickersResponse.class,
  GetFileResponse.class,
  GetGameHighScoresResponse.class,
  GetMeResponse.class,
  GetMyCommandsResponse.class,
  GetMyDefaultAdministratorRightsResponse.class,
  GetStickerSetResponse.class,
  GetUpdatesResponse.class,
  GetUserProfilePhotosResponse.class,
  GetWebhookInfoResponse.class,
  MessageIdResponse.class,
  MessagesResponse.class,
  PollResponse.class,
  SendResponse.class,
  SentWebAppMessageResponse.class,
  StringResponse.class,

  // Telegram Models - misc
  AttachName.class,

}, ignoreNested = false)
public class ReflectionConfig extends BaseReflectionConfig {
}
