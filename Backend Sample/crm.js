const request = require("request");
const printAll = require("./PrintAll");
const nodemailer = require('nodemailer');
const eventsCheck = require("./EventsCheck");
const StringsEnglish = require('../config/StringsEnglish');
const Nexmo = require('nexmo');
const logging = require('./logging')
class CRM {
    static saleDoneTextUser(req,user, data, callback) {
      const clientModel = req.app.get('clientModel');
      const UserDetails = require("../models/userdetails")(clientModel);
      const User = require("../models/user")(clientModel);
      UserDetails.getAllUserDetailsById( data.id == user.id ? [user.id] : [user.id, data.id], function(err, ad){
        callback()
        if (!err) {
          var multUserDets = []
          var userhandle = null;
          if (ad && ad.length > 0) { // should be true
            if (ad.length > 1) {
                multUserDets = [ad[0].id == user.id ? ad[0] : ad[1]]
                userhandle = (ad[0].id == data.id ? ad[0] : ad[1]).userhandle
            } else {
              multUserDets = data.id == user.id ? ad : [{id: user.id}] // no details but send email
              userhandle = ad[0].userhandle
            }
            CRM.phoneSendData(req,user,multUserDets,data, userhandle)
          }
        }
      })
    }
    static userRegistered(creatorUserHandle, phone, email, numAttendees, priceToUse, data) {
      numAttendees = Math.max(numAttendees, 1)
      var txtMessage = null;
      var emailMessageText = null;
      var emailMessageHtml = null;
      if (!data.price || data.price <= 0.0) {
        if (priceToUse && priceToUse > 0.0) {
          txtMessage = "Someone just got a paid ticket to your LIVE! They paid $" + priceToUse + ". You are now at " + numAttendees + " attendee" + (numAttendees == 1 ? "" : "s") + ". Keep promoting to get more awesome texts like this!";
          emailMessageText = "Hi " + creatorUserHandle + "!\n\nSomeone just got a paid ticket to your LIVE! They paid $" + priceToUse + ". You are now at " + numAttendees + " attendee" + (numAttendees == 1 ? "" : "s") + ". Keep promoting to get more awesome emails like this!\n\nYou rock!\n-myLIVE team";
          emailMessageHtml = "Hi " + creatorUserHandle + "!<br/><br/>Someone just got a paid ticket to your LIVE! They paid $" + priceToUse + ". You are now at " + numAttendees + " attendee" + (numAttendees == 1 ? "" : "s") + ". Keep promoting to get more awesome emails like this!<br/><br/>You rock!<br/>-myLIVE team";
        } else {
          txtMessage = "Someone just got a free ticket to your LIVE! You are now at " + numAttendees + " attendee" + (numAttendees == 1 ? "" : "s") + ". Keep promoting to get more awesome texts like this!";
          emailMessageText = "Hi " + creatorUserHandle + "!\n\nSomeone just got a free ticket to your LIVE! You are now at " + numAttendees + " attendee" + (numAttendees == 1 ? "" : "s") + ". Keep promoting to get more awesome emails like this!\n\nYou rock!\n-myLIVE team";
          emailMessageHtml = "Hi " + creatorUserHandle + "!<br/><br/>Someone just got a free ticket to your LIVE! You are now at " + numAttendees + " attendee" + (numAttendees == 1 ? "" : "s") + ". Keep promoting to get more awesome emails like this!<br/><br/>You rock!<br/>-myLIVE team";
        }
      } else {
        txtMessage = "Someone just got a ticket to your LIVE! You are now at " + numAttendees + " attendee" + (numAttendees == 1 ? "" : "s") + ". Keep promoting to get more awesome texts like this!";
        emailMessageText = "Hi " + creatorUserHandle + "!\n\nSomeone just got a ticket to your LIVE! You are now at " + numAttendees + " attendee" + (numAttendees == 1 ? "" : "s") + ". Keep promoting to get more awesome emails like this!\n\nYou rock!\n-myLIVE team";
        emailMessageHtml = "Hi " + creatorUserHandle + "!<br/><br/>Someone just got a ticket to your LIVE! You are now at " + numAttendees + " attendee" + (numAttendees == 1 ? "" : "s") + ". Keep promoting to get more awesome emails like this!<br/><br/>You rock!<br/>-myLIVE team";
      }
      var emailSubject = "Someone bought a ticket for your LIVE!";
      if (!process.env.emailFrom || !process.env.emailPassword) {
        printAll("should not happen api secrets dont exist", creatorUserHandle, phone, email)
      } else {
        if (email) {
          var transporter = nodemailer.createTransport({
            service: 'gmail',
            auth: {
              user: process.env.emailFrom,
              pass: process.env.emailPassword
            }
          });
          var mailOptions = {
            from: process.env.emailFrom,
            to: email,
            subject: emailSubject,
            text: emailMessageText,
            html: emailMessageHtml,
          };
          transporter.sendMail(mailOptions, function(error, info){
            if (error) {
              logging.log(logging.emailCRMIdentifier(), error)
            }
            printAll(error, info)
          });
        }
      }
      if (!process.env.textApiApiKey || !process.env.textApiApiSecret || !process.env.textSendFrom) {
        printAll("should not happen api secrets dont exist", creatorUserHandle, phone, email)
      } else {
        if (phone) {
          const nexmo = new Nexmo({
            apiKey: process.env.textApiApiKey,
            apiSecret: process.env.textApiApiSecret
          })
          const from = process.env.textSendFrom
          nexmo.message.sendSms(from, phone, txtMessage, (err, responseData) => {
            try {
              if (err) {
                  logging.log(logging.textCRMIdentifier(), err)
              } else {
                  if(!(responseData.messages[0]['status'] === "0")) {
                      logging.log(logging.textCRMIdentifier(), responseData.messages[0]['error-text'])
                  }
              }
            } catch (e) {
              // ignore as somethign wrong with the form
            }
          })
        }
      }
    }
    static phoneSendData(req,user,multUserDets, data, creatorUserHandle = null, doNotEmail = false) {
      // creator receiving msg about own event,
      // consumer register w/o link,
      // consumer register w link,
      // zoom link arrived 10 min early,
      // meeting started
      const clientModel = req.app != null ? req.app.get('clientModel') : req.clientModel;
      const User = require("../models/user")(clientModel);
      printAll(user,multUserDets, data)
      var txtMessage = null;
      var emailMessageText = null;
      var emailMessageHtml = null;
      var emailSubject = null;
      const diffMs = new Date(data.timedate) - new Date();
      const diffMins = Math.floor(diffMs / 60000)
      const zoomStateStart = !data.zoomlink ? null : (diffMins < 2 ? true : false)
      const zoomMeetingHost = (user && user.id == data.id) || (multUserDets[0].id == data.id)
      function createLink(text, link = null){
        if (link != null) {
          if (link == "ZOOM") {
            return "<a href=\'" + encodeURI("https://www.zoom.com/") + "\'>" + text + "</a>";
          } else {
            return "<a href=\'" + encodeURI(link) + "\'>" + text + "</a>";
          }
        } else {
            return "<a href=\'" + encodeURI(text) + "\'>" + text + "</a>";
        }
      }
      if (zoomStateStart) {
        if (zoomMeetingHost) {
          txtMessage = "Your LIVE has started! Here is your link to join: " + eventsCheck.getLiveLink(creatorUserHandle)
          emailSubject = "Your LIVE just started!"
          emailMessageText = "Your LIVE has started! Here is your link to join: " + eventsCheck.getLiveLink(creatorUserHandle) + ".\n\nEnjoy!\n-myLIVE team"
          emailMessageHtml = "Your LIVE has started! Here is your link to join: " + createLink(eventsCheck.getLiveLink(creatorUserHandle)) + ".<br/><br/>Enjoy!<br/>-myLIVE team"
        } else {
          txtMessage = creatorUserHandle + " just started the LIVE you registered for. Join them at: " + eventsCheck.getLiveLink(creatorUserHandle)
          emailSubject = creatorUserHandle + " just started the LIVE"
          emailMessageText = creatorUserHandle + " just started the LIVE you registered for. Join them at: " + eventsCheck.getLiveLink(creatorUserHandle) + "\n\nEnjoy!\n-myLIVE team"
          emailMessageHtml = creatorUserHandle + " just started the LIVE you registered for. Join them at: " + createLink(eventsCheck.getLiveLink(creatorUserHandle)) + "<br/><br/>Enjoy!<br/>-myLIVE team"
        }
      } else {
        if (diffMins > 30) {
          if (zoomMeetingHost) {
            txtMessage = "Just a reminder that your LIVE \"" + data.title + "\" is coming up!" + " Here\'s the link you will use to start it: " + eventsCheck.getLiveLink(creatorUserHandle)
            emailSubject = "Your upcoming LIVE!"
            emailMessageText = "Just a reminder that your LIVE \"" + data.title + "\" is coming up!" + " Here\'s the link to start the LIVE: " + eventsCheck.getLiveLink(creatorUserHandle) + "\n\nGood Have a great day!\n-myLIVE team"
            emailMessageHtml = "Just a reminder that your LIVE \"" + data.title + "\" is coming up!" + " Here\'s the link to start the LIVE: " + eventsCheck.getLiveLink(creatorUserHandle) +  "<br/><br/>Have a great day!<br/>-myLIVE team"
          } else {
            txtMessage = "LIVE with "+ creatorUserHandle + " is coming up! " + "Here\'s the link to join LIVE: " + eventsCheck.getLiveLink(creatorUserHandle)
            emailSubject = "Upcoming LIVE with " + creatorUserHandle + "!"
            emailMessageText = "Get excited, LIVE with " + creatorUserHandle + " is coming up! " + "Here\'s the link to join LIVE: " + eventsCheck.getLiveLink(creatorUserHandle) + "\n\nSee you there!\n-myLIVE team"
            emailMessageHtml = "Get excited, LIVE with " + creatorUserHandle + " is coming up! " + "Here\'s the link to join LIVE: " + createLink(eventsCheck.getLiveLink(creatorUserHandle)) + "<br/><br/>See you there!<br/>-myLIVE team"
          }
        } else {
          if (zoomMeetingHost) {
            txtMessage = "Your LIVE is starting in " + diffMins + " minutes! Here\'s the link to start the LIVE: " + eventsCheck.getLiveLink(creatorUserHandle)
            emailSubject = "Your LIVE is about to start"
            emailMessageText = "Your LIVE \"" + data.title + "\" is scheduled to start in " + diffMins + " minutes! Here\'s the link to start the LIVE: " + eventsCheck.getLiveLink(creatorUserHandle) + "\n\nGood luck!\n-myLIVE team"
            emailMessageHtml = "Your LIVE \"" + data.title + "\" is scheduled to start in " + diffMins + " minutes! Here\'s the link to start the LIVE: " + createLink(eventsCheck.getLiveLink(creatorUserHandle)) + "<br/><br/>Good luck!<br/>-myLIVE team"
          } else {
            txtMessage = "LIVE with "+ creatorUserHandle + " is starting in " + diffMins + " minutes! Here\'s the link to join LIVE: " + eventsCheck.getLiveLink(creatorUserHandle)
            emailSubject = "LIVE is about to start!"
            emailMessageText = "Get excited, LIVE with " + creatorUserHandle + " is starting in " + diffMins + " minutes! Here\'s the link to join LIVE: " + eventsCheck.getLiveLink(creatorUserHandle) + "\n\nSee you there!\n-myLIVE team"
            emailMessageHtml = "Get excited, LIVE with " + creatorUserHandle + " is starting in " + diffMins + " minutes! Here\'s the link to join LIVE: " + createLink(eventsCheck.getLiveLink(creatorUserHandle)) + "<br/><br/>See you there!<br/>-myLIVE team"
          }
        }
      }
      if (!doNotEmail) {
        if (!process.env.emailFrom || !process.env.emailPassword) {
          printAll("should not happen api secrets dont exist", emailMessageText,emailMessageHtml, user, multUserDets, data)
        } else {
          var transporter = nodemailer.createTransport({
            service: 'gmail',
            auth: {
              user: process.env.emailFrom,
              pass: process.env.emailPassword
            }
          });
          var allUserIds = []
          for (var i = 0; i < multUserDets.length; i++) {
            allUserIds.push(multUserDets[i].id)
          }
        User.getAllUserById( allUserIds, function(err, dbRes){
          if (dbRes) {
              for (var i = 0; i < dbRes.length; i++) {
                setTimeout(function(toEmail){
                  var mailOptions = {
                    from: process.env.emailFrom,
                    to: toEmail,
                    subject: emailSubject,
                    text: emailMessageText,
                    html: emailMessageHtml,
                  };
                  transporter.sendMail(mailOptions, function(error, info){
                    if (error) {
                      logging.log(logging.emailCRMIdentifier(), error)
                    }
                    printAll(error, info)
                  });
                },i*2000,dbRes[i].email)
              }
          }
        })
      }
    }


      if (!process.env.textApiApiKey || !process.env.textApiApiSecret || !process.env.textSendFrom) {
        printAll("should not happen api secrets dont exist", txtMessage, user, multUserDets, data)
      } else {
        const nexmo = new Nexmo({
          apiKey: process.env.textApiApiKey,
          apiSecret: process.env.textApiApiSecret
        })
        for (var i = 0; i < multUserDets.length; i++) {
          if (!multUserDets[i].phone) {
            continue
          }
          const from = process.env.textSendFrom
          setTimeout(function(to){
            nexmo.message.sendSms(from, to, txtMessage, (err, responseData) => {
              try {
                if (err) {
                    logging.log(logging.textCRMIdentifier(), err)
                } else {
                    if(!(responseData.messages[0]['status'] === "0")) {
                        logging.log(logging.textCRMIdentifier(), responseData.messages[0]['error-text'])
                    }
                }
              } catch (e) {
                // ignore as somethign wrong with the form
              }
            })
          },i*2000,multUserDets[i].phone)
        }
      }
    }

}

module.exports = CRM;
