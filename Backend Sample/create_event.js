const express = require("express");
const router = express.Router();
const ValidateInput = require("../../StaticModules/ValidateInput");
const crm = require("../../StaticModules/crm")
const StringsEnglish = require('../../config/StringsEnglish');
const emailself = require("../../StaticModules/emailself")
const printAll = require("../../StaticModules/PrintAll");
router.post("/", function(req, res, next) {
  const clientModel = req.app.get('clientModel');
  const LiveVideo = require('../../models/livevideo')(clientModel);
  const userSignIn = require("../../StaticModules/UserSignIn")(clientModel);
  const UserDetails = require("../../models/userdetails")(clientModel);
  // console.log("7 line", req.body);
  userSignIn.getUser(req,function(user){
    const title = req.body.title;
    const price = parseFloat(req.body.price);
    const description = req.body.description;
    const duration = parseInt(req.body.duration);
    const TimeDate = parseInt(req.body.TimeDate);
    printAll("13 line",user, title, price, description, duration, TimeDate);
    if (ValidateInput.titleIsInvalid(title) || ValidateInput.priceIsInvalid(price) || ValidateInput.descriptionIsInvalid(description) || ValidateInput.durationIsInvalid(duration) || ValidateInput.TimeDateIsInvalid(TimeDate)) {
      res.set("Content-Type", 'application/json');
      res.send({error: StringsEnglish.eventInputIsInvalid()});
    } else {
      if (!user) {
        // refresh since not signed in
        res.set("Content-Type", 'application/json');
        res.send({refresh: true});
      } else {
        ValidateInput.addEventIsInvalid(req, user, function(isInvalid){
          if (isInvalid) {
            // refresh to show still upcoming
            res.set("Content-Type", 'application/json');
            res.send({refresh: true});
          } else {
            LiveVideo.addEvent(user.id,price,duration,TimeDate,title,description,function(err,data){
              // console.log("line 22",err, data);
              if (err) {
                res.set("Content-Type", 'application/json');
                res.send({error: StringsEnglish.eventInputServerError()});
              } else {
                const eventreminder = require("../../StaticModules/eventreminder.js")(clientModel);
                eventreminder.remind(data)
                emailself.emailselfFn(StringsEnglish.emailEventCreatedSubject(),JSON.stringify({'user': user.email, 'id': data.id, 'liveid': data.liveid, 'price': data.price, 'duration': data.duration, 'timedate': data.timedate, 'title': data.title, 'description': data.description}))
                UserDetails.getUserDetailsById(user.id,function(err, multUserDets){
                  // ignore error
                  if (err) {
                    res.set("Content-Type", 'application/json');
                    res.send({refresh: true});
                  } else {
                    crm.phoneSendData(req,user, [multUserDets], data, multUserDets.userhandle)
                    res.set("Content-Type", 'application/json');
                    res.send({refresh: true});
                  }
                })
              }
            })
          }
        })
      }
    }
  })
})
module.exports = router;
