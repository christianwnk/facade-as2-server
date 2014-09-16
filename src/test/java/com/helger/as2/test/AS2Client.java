/**
 * The FreeBSD Copyright
 * Copyright 1994-2008 The FreeBSD Project. All rights reserved.
 * Copyright (C) 2013-2014 Philip Helger philip[at]helger[dot]com
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *    1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE FREEBSD PROJECT ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE FREEBSD PROJECT OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation
 * are those of the authors and should not be interpreted as representing
 * official policies, either expressed or implied, of the FreeBSD Project.
 */
package com.helger.as2.test;

import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.as2.cert.ServerPKCS12CertificateFactory;
import com.helger.as2lib.cert.PKCS12CertificateFactory;
import com.helger.as2lib.exception.OpenAS2Exception;
import com.helger.as2lib.message.AS2Message;
import com.helger.as2lib.message.IMessage;
import com.helger.as2lib.partner.CPartnershipIDs;
import com.helger.as2lib.partner.Partnership;
import com.helger.as2lib.partner.SelfFillingPartnershipFactory;
import com.helger.as2lib.processor.sender.IProcessorSenderModule;
import com.helger.as2lib.session.Session;
import com.helger.as2lib.util.StringMap;
import com.helger.commons.annotations.UnsupportedOperation;

public class AS2Client
{
  private static final Logger s_aLogger = LoggerFactory.getLogger (AS2Client.class);

  @Nonnull
  private static Partnership _buildPartnership (@Nonnull final ConnectionSettings aSettings)
  {
    final Partnership aPartnership = new Partnership (aSettings.partnershipName);

    aPartnership.setAttribute (CPartnershipIDs.PA_AS2_URL, aSettings.receiverAs2Url);
    aPartnership.setReceiverID (CPartnershipIDs.PID_AS2, aSettings.receiverAs2Id);
    aPartnership.setReceiverID (CPartnershipIDs.PID_X509_ALIAS, aSettings.receiverKeyAlias);

    aPartnership.setSenderID (CPartnershipIDs.PID_AS2, aSettings.senderAs2Id);
    aPartnership.setSenderID (CPartnershipIDs.PID_X509_ALIAS, aSettings.senderKeyAlias);
    aPartnership.setSenderID (Partnership.PID_EMAIL, aSettings.senderEmail);

    aPartnership.setAttribute (CPartnershipIDs.PA_AS2_MDN_OPTIONS, aSettings.mdnOptions);

    aPartnership.setAttribute (CPartnershipIDs.PA_ENCRYPT,
                               aSettings.encrypt == null ? null : aSettings.encrypt.getID ());
    aPartnership.setAttribute (CPartnershipIDs.PA_SIGN, aSettings.sign == null ? null : aSettings.sign.getID ());
    aPartnership.setAttribute (Partnership.PA_PROTOCOL, "as2");
    // partnership.setAttribute(AS2Partnership.PA_AS2_MDN_TO,"http://localhost:10080");
    aPartnership.setAttribute (CPartnershipIDs.PA_AS2_RECEIPT_OPTION, null);

    aPartnership.setAttribute (CPartnershipIDs.PA_MESSAGEID, aSettings.messageIDFormat);
    return aPartnership;
  }

  @Nonnull
  private static IMessage _buildMessage (@Nonnull final Partnership aPartnership, @Nonnull final AS2Request aRequest) throws MessagingException,
                                                                                                                     OpenAS2Exception
  {
    final AS2Message aMsg = new AS2Message ();
    aMsg.setContentType (aRequest.getContentType ());
    aMsg.setSubject (aRequest.getSubject ());
    aMsg.setPartnership (aPartnership);
    aMsg.setMessageID (aMsg.generateMessageID ());

    aMsg.setAttribute (CPartnershipIDs.PA_AS2_URL, aPartnership.getAttribute (CPartnershipIDs.PA_AS2_URL));
    aMsg.setAttribute (CPartnershipIDs.PID_AS2, aPartnership.getReceiverID (CPartnershipIDs.PID_AS2));
    aMsg.setAttribute (Partnership.PID_EMAIL, aPartnership.getSenderID (Partnership.PID_EMAIL));

    // Build message content
    final MimeBodyPart aPart = new MimeBodyPart ();
    aRequest.applyDataOntoMimeBodyPart (aPart);
    aMsg.setData (aPart);

    return aMsg;
  }

  // TODO do object
  // TODO extract interface
  public static AS2Response sendSynchronous (@Nonnull final ConnectionSettings aSettings,
                                             @Nonnull final AS2Request aRequest)
  {
    final AS2Response aResponse = new AS2Response ();
    IMessage aMsg = null;
    try
    {
      final Partnership aPartnership = _buildPartnership (aSettings);

      aMsg = _buildMessage (aPartnership, aRequest);
      aResponse.originalMessageId = aMsg.getMessageID ();

      // logger.info("msgId to send: "+msg.getMessageID());

      final Session aSession = new Session ();
      {
        final StringMap aParams = new StringMap ();
        aParams.setAttribute (PKCS12CertificateFactory.PARAM_FILENAME, aSettings.p12FilePath);
        aParams.setAttribute (PKCS12CertificateFactory.PARAM_PASSWORD, aSettings.p12FilePassword);

        final ServerPKCS12CertificateFactory aCertFactory = new ServerPKCS12CertificateFactory ();
        aCertFactory.initDynamicComponent (aSession, aParams);
        aSession.setCertificateFactory (aCertFactory);
      }

      final SelfFillingPartnershipFactory aPartnershipFactory = new SelfFillingPartnershipFactory ();
      aSession.setPartnershipFactory (aPartnershipFactory);

      final TestSenderModule aSender = new TestSenderModule ();
      aSender.initDynamicComponent (aSession, null);
      aSender.handle (IProcessorSenderModule.DO_SEND, aMsg, null);
    }
    catch (final Exception ex)
    {
      s_aLogger.error (ex.getMessage (), ex);
      aResponse.isError = true;
      aResponse.exception = ex;
      aResponse.errorDescription = ex.getMessage ();
    }
    finally
    {
      if (aMsg != null && aMsg.getMDN () != null)
      {
        aResponse.receivedMdnId = aMsg.getMDN ().getMessageID ();
        aResponse.text = aMsg.getMDN ().getText ();
        aResponse.disposition = aMsg.getMDN ().getAttribute ("DISPOSITION");
      }
    }

    s_aLogger.info (aResponse.toString ());

    return aResponse;
  }

  /**
   * @param settings
   *        Settings
   * @param request
   *        Request
   * @return UnsupportedOperationException
   */
  @UnsupportedOperation
  public AS2Response sendAsync (final ConnectionSettings settings, final AS2Request request)
  {
    throw new UnsupportedOperationException ();
    // Response response = null;
    // return response;
  }

  /**
   * @param settings
   *        Settings
   * @param stream
   *        Input stream
   * @return UnsupportedOperationException
   */
  @UnsupportedOperation
  public AS2Response processAsyncReply (final ConnectionSettings settings, final InputStream stream)
  {
    throw new UnsupportedOperationException ();
    // Response response = null;
    // return response;
  }
}
