const logging = require("../StaticModules/logging")
const StringsEnglish = require('../config/StringsEnglish');
class Attendance {
   constructor(clientModel) {
       this.clientModel = clientModel;
       this.dbPreProcess = require("./db")(clientModel)
   }
   getAllAttendee(liveid,callback) {
     const text = 'SELECT * FROM ATTENDANCE WHERE liveid = $1';
     const values = [liveid];
     this.clientModel.query(text, values, (err, dbRes) => {
       dbRes = this.dbPreProcess.preProcessDbRes(dbRes)
       if (err) {
         logging.log(logging.generalDBIdentifier(), err)
         callback(err, null);
       } else {
         callback(null, dbRes)
       }
     });
   }

   getNumAttendees(data,callback) {
     var dataSpecific = [];
     for (var i = 0; i < data.length; i++) {
       dataSpecific.push(data[i].liveid)
     }
     const text = 'SELECT COUNT(id) as idc, liveid FROM ATTENDANCE WHERE liveid = ANY ($1) GROUP BY liveid;';
     const values = [dataSpecific];
     this.clientModel.query(text, values, (err, dbRes) => {
       dbRes = this.dbPreProcess.preProcessDbRes(dbRes)
       // console.log("attendance", err, dbRes)
       if (err) {
         logging.log(logging.generalDBIdentifier(), err)
         callback(err, null);
       } else {
         var retThis = {}
         for (var i = 0; i < dbRes.length; i++) {
           retThis[dbRes[i].liveid] = parseInt(dbRes[i].idc)
         }
         // console.log("retThis 33", retThis);
         for (var i = 0; i < dataSpecific.length; i++) {
           if (!retThis[dataSpecific[i]]) {
             retThis[dataSpecific[i]] = 0
           }
         }
         // console.log("retThis 44", retThis);
         callback(null, retThis)
       }
     });
   }

   register(id, liveid, callback) {
     const text = 'INSERT INTO ATTENDANCE(id, liveid) VALUES($1, $2)';
     const values = [id, liveid];
     this.clientModel.query(text, values, (err, dbRes) => {
       dbRes = this.dbPreProcess.preProcessDbRes(dbRes)
       if (err) {
         logging.log(logging.generalDBIdentifier(), err)
         callback(err);
       } else {
         callback(null)
       }
     });
   }

   didPurchase(id, liveid,callback) {
     const text = 'SELECT * FROM ATTENDANCE WHERE liveid = $1 AND id = $2';
     const values = [liveid, id];
     this.clientModel.query(text, values, (err, dbRes) => {
       dbRes = this.dbPreProcess.preProcessDbRes(dbRes)
       if (err) {
         logging.log(logging.generalDBIdentifier(), err)
         callback(err, null);
       } else {
         callback(null, dbRes.length > 0)
       }
     });
   }
}
module.exports = ( arg1 ) => { return new Attendance(arg1) };
