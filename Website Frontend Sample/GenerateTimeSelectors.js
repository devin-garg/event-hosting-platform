/* global moment */
document.addEventListener("DOMContentLoaded", function() {
  // Generate time selectors
  var startTimeInput = document.querySelector("#start-time-input");
  var endTimeInput = document.querySelector("#end-time-input");
  var startTimeList = document.querySelector('#start-time-list');
  var endTimeList = document.querySelector('#end-time-list');
  var startTimeLabel = document.querySelector('#start-time-label');
  var endTimeLabel = document.querySelector('#end-time-label');
  var startTimeDisplay = document.querySelector('#start-time-disp');
  var endTimeDisplay = document.querySelector('#end-time-disp');

  var counter = moment().startOf('day');
  var curr = moment()
  curr.add(15, 'minutes');
  var currentHour = curr.hour();
  var currentMinute = curr.minute();

  // Generate content for start time selector, and also initial end time selector content
  // End time order will change after selecting start time

  for (var quarter = 0; quarter < 24 * 4; quarter++) {
    var value = moment(counter);
    counter.add(15, 'minutes');
    var hourAsText = value.format('hh:mm a');

    var startOption = document.createElement('li');
    var endOption = document.createElement('li');

    var hour = value.hour();
    var minute = value.minute();

    startOption.textContent = hourAsText;
    startOption.setAttribute('data-hour', hour.toString());
    startOption.setAttribute('data-minute', minute.toString());
    startOption.addEventListener('click', handleClickStart);

    if ((hour < currentHour) || (hour === currentHour && minute < currentMinute)) {
      startOption.classList.add('time-list-hide-if-today')
    }

    endOption.textContent = hourAsText;
    endOption.setAttribute('data-hour', hour.toString());
    endOption.setAttribute('data-minute', minute.toString());
    endOption.addEventListener('click', handleClickEnd);

    startTimeList.appendChild(startOption);
    endTimeList.appendChild(endOption);
  }

  var openStartList = function() {
    startTimeList.classList.add('open');
  };
  var openEndList = function() {
    endTimeList.classList.add('open');
  };
  startTimeDisplay.addEventListener('click', openStartList);
  startTimeLabel.addEventListener('click', openStartList);
  endTimeDisplay.addEventListener('click', openEndList);
  endTimeLabel.addEventListener('click', openEndList);

  function handleClickStart() {
    startTimeInput.value = this.textContent;
    startTimeDisplay.textContent = this.textContent;
    startTimeInput.classList.add('filled');
    startTimeList.classList.remove('open');
    regenerateEndTimes(this);

    // if the selected hour/minute is before current hour/minute,
    // disallow setting date to current day
    if (window.setPickerAllowSinceTomorrow){
      window.setPickerAllowSinceTomorrow(
        parseInt(this.getAttribute('data-hour')),
        parseInt(this.getAttribute('data-minute'))
      )
    }
    regenerateEndTimes(this);
  }
  function handleClickEnd() {
    endTimeInput.value = this.textContent;
    endTimeDisplay.textContent = this.textContent;
    endTimeInput.classList.add('filled');
    endTimeList.classList.remove('open');
  }

  // Regenerates end times starting from 15 minutes after selected start time
  function regenerateEndTimes(startTimeElem) {
    var startHour = parseInt(startTimeElem.getAttribute('data-hour'));
    var startMinute = parseInt(startTimeElem.getAttribute('data-minute'));

    var counter = moment().startOf('day');
    counter.hour(startHour);
    counter.minute(startMinute);

    var oldEndOptions = document.querySelectorAll('#end-time-list > li');
    for (var i=0; i < oldEndOptions.length; i++) {
      endTimeList.removeChild(oldEndOptions[i]);
    }

    for (var quarter = 0; quarter < 24 * 4; quarter++) {
      counter.add(15, 'minutes');
      var value = moment(counter);

      var endOption = document.createElement('li');
      endOption.textContent = value.format('hh:mm a');
      endOption.setAttribute('data-hour', value.hour().toString());
      endOption.setAttribute('data-minute', value.minute().toString());
      endOption.addEventListener('click', handleClickEnd);

      endTimeList.appendChild(endOption);
    }
  }

  // Detects clicking away
  document.addEventListener('click', function(ev) {
    if (!ev.target.closest('#start-time-wrapper')) {
      startTimeList.classList.remove('open');
    }
    if (!ev.target.closest('#end-time-wrapper')) {
      endTimeList.classList.remove('open');
    }
  });
});
