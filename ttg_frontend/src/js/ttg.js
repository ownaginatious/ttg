var course_color = '#CCFF9A';
var conflict_color = '#FABD4E';

// Settings for the particular school.
var university = null;

var DataLoader = {

    universities : null,
    timingMap : null,
    reloadData : null,

    buildTimingMap : function () {

        DataLoader.timingMap = [];

        var days = ['mo', 'tu', 'we', 'th', 'fr', 'sa'];

        for (var term = 1; term < 3; term++) {
            for (var day = 0; day < days.length; day++) {

                var last_hour = 0;
                var last_minute = 0;
                var hour = 8;
                var minute = 0;

                // Build depth map.
                while ((hour === 22 && minute === 0) || (hour < 22)) {

                    var time_slot = "term" + term.toString() + "_" + days[day] +
                                    "_" + hour.toString() + minute.toString();

                    DataLoader.timingMap.push(time_slot);

                    if (minute === 30) {
                        minute = 0;
                        hour++;
                    } else {
                        minute = 30;
                    }
                }
            }
        }
    },

    retrieveUniversities : function() {

        $.ajax({
            type: "GET",
            url: "static/data/universities.json",
            dataType: "json",
            success: function(response) {

                $("#courses_div").html("");
                $("#course_add_button").html("<b>Click to load university data</b>");
                $("#course_add_button").attr("onclick", "DataLoader.setUniversity();");
                $("#course_add_button").attr("disabled", false);

                DataLoader.universities = response;

                TimeTabler.renderSchedule();

                var selector = $('<select></select>').attr("id", "university_selector");

                selector.append(
                    $('<option></option>').html('Select a university...')
                        .attr({
                            'selected' : 'selected',
                            'value' : 'bad'
                        })
                );

                var ids = [];

                // Sort the universities.
                for (var university in response) {
                    ids.push(university);
                }

                ids.sort();

                for (var x = 0; x < ids.length; x++) {
                    selector.append(
                        $('<option></option>')
                            .html(response[ids[x]].name)
                            .attr('value', ids[x])
                    );
                }

                $("#courses_div").append(selector);

                DataLoader.injectStateData();
            },
            error: function(jqXHR, textStatus, errorThrown) {
                alert("Failed to load supported university data. Please " +
                      "contact the admin.");
            }
        });
    },

    createStateLink : function() {

        $("#state_create_button").attr("disabled", true);
        $("#state_create_button").html("Hold on...");

        var selectors = [];

        for (var active = 0; active < BoxManager.activeSelectors.length; active++) {

            var id = BoxManager.activeSelectors[active];

            if ($("#advance_button_" + id).attr("disabled") === "disabled") {

                id = "_select_" + BoxManager.activeSelectors[active] +
                     " option:selected";

                var selection_array = [];

                selection_array.push($("#dep" + id ).attr("value"));
                selection_array.push($("#course" + id).attr("value"));
                selection_array.push($("#core" + id).attr("value"));
                selection_array.push($("#tutorial" + id).attr("value"));
                selection_array.push($("#lab" + id).attr("value"));

                selectors.push(selection_array);
            }
        }

        var schedule_data = {
            "selectors" : selectors,
            "school" : university.id
        };
        $("#timetable_link").html("Saving...");
        $.ajax({
            type: "POST",
            url: "/api/v1/schedule",
            dataType: "json",
            contentType: "application/json",
            data : JSON.stringify(schedule_data),
            success: function(response){

                var stateLink = "https://ttg.fyi/#" + response.id;

                $("#timetable_link").html(
                    $("<a></a>").html(stateLink).attr("href", stateLink)
                );

                $("#state_create_button").html("Create Link");
                $("#state_create_button").attr("disabled", false);
            },
            error: function(jqXHR, textStatus, errorThrown){
                $("#timetable_link").html("Error (" + errorThrown +
                                          "). Try again in a few minutes, or " +
                                          "contact the admin if the issue " +
                                          "persists.");
            }
        });
    },

    injectStateData :  function() {
        key = window.location.href.match(/#([a-zA-Z0-9]+)$/);
        if (key){
            $.ajax({
                type: "GET",
                url: "/api/v1/schedule/" + key[1],
                dataType: "json",
                success: function(response){
                    BoxManager.reconstructStage1(response.data);
                },
                error: function(jqXHR, textStatus, errorThrown){
                    if (jqXHR.status === 404) {
                        alert("No such schedule with key \"" + key[1] + "\"");
                    } else {
                        alert("Failed to load saved schedule (error: " +
                              errorThrown + "). Please contact the system admin.");
                    }
                }
            });
        }
    },

    getQueryParameters : function() {

        var vars = {};
        var parts = window.location.href.replace(/[?&]+([^=&]+)=([^&]*)/gi,
            function(m,key,value) {
                vars[key] = value;
            });
        return vars;
    },

    setUniversity : function() {

        var universityId = null;

        if (!DataLoader.reloadData) {
            universityId = $("#university_selector option:selected").attr('value');
        } else {
            universityId = DataLoader.reloadData.school;
        }

        if (universityId === "bad") {
            alert("Select a university before proceeding...");
            return;
        }

        university = DataLoader.universities[universityId];
        university.id = universityId;

        $("#courses_div").html(
          "Hold on, retrieving scheduling for " + university.name + "...");

        $("#course_add_button").html("<b>Add another course</b>");
        $("#course_add_button").attr("onclick",
            "BoxManager.addNewSelector(TimeTabler.masterList)");
        $("#course_add_button").attr("disabled", true);

        $.ajax({
            type: "GET",
            url: "/api/v1/school/" + universityId,
            dataType: "json",
            success: function(response) {
                $("#course_add_button").attr("disabled", false);
                $("#courses_div").html("<b>University</b> : " +
                    university.name + "</br>");

                TimeTabler.masterCourseList = response.courses;
                TimeTabler.masterDepartmentList = response.departments;

                if (DataLoader.reloadData) {
                    BoxManager.reconstructStage2(DataLoader.reloadData);
                } else {
                    BoxManager.addNewSelector(TimeTabler.masterList);
                }
                $("#save_state_div").css("display", "block");
            },
            error: function(jqXHR, textStatus, errorThrown) {
                alert("Failed to load school data. Please contact the admin.");
            }
        });
    }
};

var TimeTabler = {

    runningSchedule : {},
    masterCourseList : null,
    masterDepartmentList : null,

    sameCourse : function(schoolUnitA, schoolUnitB) {

        if (schoolUnitA.term !== schoolUnitB.term) {
            if (!schoolUnitA.termThree || !schoolUnitB.termThree) {
                return false;
            }
        }

        if (schoolUnitA.name !== schoolUnitB.name) {
            return false;
        }

        if (schoolUnitA.code !== schoolUnitB.code) {
            return false;
        }

        if (schoolUnitA.dep !== schoolUnitB.dep) {
            return false;
        }

        return true;
    },

    schoolUnitsEqual : function(schoolUnitA, schoolUnitB) {

        if (schoolUnitA.targetType !== schoolUnitB.targetType){
            return false;
        }

        return TimeTabler.sameCourse(schoolUnitA, schoolUnitB);
    },

    splitTargets : function(schoolUnit) {

        var firstTerm = jQuery.extend(true, {}, schoolUnit);
        var secondTerm = jQuery.extend(true, {}, schoolUnit);

        firstTerm.target.times = [];
        secondTerm.target.times = [];

        firstTerm.term = 1;
        secondTerm.term = 2;

        firstTerm.termThree = true;
        secondTerm.termThree = true;

        var times = schoolUnit.target.times;

        for (var x = 0; x < times.length; x++) {

            if (times[x][0] === 1 || times[x][0] === 3) {
                firstTerm.target.times.push(times[x]);
            }

            if (times[x][0] === 2 || times[x][0] === 3) {
                secondTerm.target.times.push(times[x]);
            }
        }
        return {"first" : firstTerm, "second" : secondTerm};
    },

    importSchoolUnit : function(schoolUnit) {

        if (schoolUnit.term === 3) {

            var targets = TimeTabler.splitTargets(schoolUnit);

            TimeTabler.importSchoolUnit(targets.first);
            TimeTabler.importSchoolUnit(targets.second);

            return;
        }

        var term_prefix = "term" + schoolUnit.term.toString();

        var times = schoolUnit.target.times;

        for (var x = 0; x < times.length; x++) {

            // The time-space for this course exists as an array.
            var day_prefix = times[x][1];
            var hour = times[x][2];
            var minute = times[x][3];
            var finalHour = times[x][4];
            var finalMinute = times[x][5];

            // Artificially insert the location as a 'loc' key into the target for
            // readibility in complicated code.
            schoolUnit.target.loc = times[x][6];

            // Round the starting time to the slot it begins existing in.
            if (minute !== 30 && minute !== 0) {

                if (minute > 30) {
                    if (minute - 30 > 15) {
                        minute = 0;
                        hour++;
                    } else {
                        minute = 30;
                    }
                } else {
                    if (30 - minute > 15) {
                        minute = 0;
                    } else {
                        minute = 30;
                    }
                }
            }

            while ( (hour === finalHour && minute < finalMinute) || (hour < finalHour) ) {

                var timeID = term_prefix + "_" + day_prefix + "_" + hour.toString() + minute.toString();

                var timeSlot = TimeTabler.runningSchedule[timeID];

                if (!timeSlot) {
                    TimeTabler.runningSchedule[timeID] = [schoolUnit];
                } else {
                    TimeTabler.runningSchedule[timeID].push(schoolUnit);
                }

                if (minute === 30) {
                    minute = 0;
                    hour++;
                } else {
                    minute = 30;
                }
            }
        }
    },

    removeSchoolUnit: function(schoolUnit) {

        var times = schoolUnit.target.times;

        // Remove and free up any used colors.
        for (var color in TimeTabler.colorWheel){
            if (TimeTabler.colorWheel[color]){
                if (TimeTabler.sameCourse(TimeTabler.colorWheel[color], schoolUnit)){
                    TimeTabler.colorWheel[color] = null;
                    break;
                }
            }
        }

        if (schoolUnit.term === 3) {

            var targets = TimeTabler.splitTargets(schoolUnit);

            TimeTabler.removeSchoolUnit(targets.first);
            TimeTabler.removeSchoolUnit(targets.second);

            return;
        }

        var term_prefix = "term" + schoolUnit.term.toString();

        for (var x = 0; x < times.length; x++) {

            var day_prefix = times[x][1];
            var hour = times[x][2];
            var minute = times[x][3];
            var finalHour = times[x][4];
            var finalMinute = times[x][5];

            while ((hour === finalHour && minute < finalMinute) || (hour < finalHour) ) {

                var timeID = term_prefix + "_" + day_prefix + "_" + hour.toString() + minute.toString();
                var timeSlot = TimeTabler.runningSchedule[timeID];

                if (timeSlot) {

                    var toRemove = null;

                    for (var y in timeSlot) {
                        if (TimeTabler.schoolUnitsEqual(timeSlot[y], schoolUnit)){
                            toRemove = y;
                            break;
                        }
                    }

                    TimeTabler.runningSchedule[timeID].splice(y, 1);

                    // Remove the time-space from the running schedule if it is now empty.
                    if (TimeTabler.runningSchedule[timeID].length === 0) {
                        delete TimeTabler.runningSchedule[timeID];
                    }
                }

                if (minute === 30){
                    minute = 0;
                    hour++;
                } else {
                    minute = 30;
                }
            }
        }
    },

    // The colors must be predefined rather than random for aesthetic purposes.
    colorWheel : {
                    '#DCF394' : null,
                    '#CCECF4' : null,
                    '#FFD7E3' : null,
                    '#F8FAA0' : null,
                    '#FAC892' : null,
                    '#A8A4FB' : null,
                    '#BBFDD7' : null,
                    '#A8D3A0' : null,
                    '#36FED1' : null,
                    '#68FE36' : null,
                    '#F7806F' : null,
                    '#E4C7FC' : null,
                    '#DAF7CC' : null,
                    '#8BF986' : null,
                    '#DFE19D' : null
                },

    getColor : function(schoolUnit) {

        var firstFreeColor = null;
        var setColor = null;

        // Establish a new color or retrieve an exising color for the course.
        for (var color in TimeTabler.colorWheel) {
            if (TimeTabler.colorWheel[color]) {
                if (TimeTabler.sameCourse(TimeTabler.colorWheel[color], schoolUnit)) {
                    setColor = color;
                    break;
                }
            } else {
                if (!firstFreeColor) {
                    firstFreeColor = color;
                }
            }
        }

        if (!setColor) {

            // If we have run out of colors, just use the default color.
            if (!firstFreeColor) {
                setColor = course_color;
            } else {
                TimeTabler.colorWheel[firstFreeColor] = schoolUnit;
                setColor = firstFreeColor;
            }
        }

        // If the user has selected that they desire monochrome.
        if ($("input[name=course_color_group]:checked").val() === 'mono') {
            setColor = course_color;
        }

        return setColor;
    },

    clearSchedule : function(){

        var days = ['mo', 'tu', 'we', 'th', 'fr', 'sa'];

        // Remove Saturday column headers if they exist.
        if ($("#term1_sa_header").length > 0) {
            $("#term1_sa_header").remove();
            $("#term1_details").attr("colspan", 6);
            $("#term1_title").attr("colspan", 6);
        }

        if ($("#term2_sa_header").length > 0) {
            $("#term2_sa_header").remove();
            $("#term2_details").attr("colspan", 6);
            $("#term2_title").attr("colspan", 6);
        }

        for (var i = 0; i < DataLoader.timingMap.length; i++) {

            var time_slot = DataLoader.timingMap[i];

            var day = /^term[123]_([a-z]{2,2})_[0-9]+$/.exec(time_slot)[1];

            if ($("#" + time_slot).length > 0)
                $("#" + time_slot).remove();

            if (day != 'sa'){

                var newTime = $('<td></td>').attr({'id' : time_slot, 'class': 'daytime_slot'});
                $("#" + time_slot.replace("_" + day + "_", "_")).append(newTime);
            }
        }
    },

    buildSaturday : function(term) {

        $("#term" + term.toString() + "_title").attr("colspan", 7);
        $("#term" + term.toString() + "_details").attr("colspan", 7);

        var newDayHeader = $('<th>Saturday</th>').attr({'id' : "term" + term.toString() + "_sa_header" , 'class': 'days'});
        $("#term" + term.toString() + "_days").append(newDayHeader);

        var pattern = new RegExp("^term" + term + "_sa_");

        for (var i = 0; i < DataLoader.timingMap.length; i++) {

            var time_slot = DataLoader.timingMap[i];

            if (pattern.exec(time_slot)) {
                var newTime = $('<td></td>').attr({'id' : time_slot, 'class': 'daytime_slot'});
                $("#" + time_slot.replace("_sa_", "_")).append(newTime);
            }
        }
    },

    renderSchedule : function() {

        var i = 0;
        var j = 0;
        var k = 0;
        var supervisor = null;
        var setColor = null;

        var days = ['mo', 'tu', 'we', 'th', 'fr', 'sa'];

        var term_hours = [0,0];
        var term_units = [0,0];

        var saturday_built = [false, false];

        var last_conflict_array = null; // The last array of conflicting school units.
        var conflict_element = null;    // The last element where a conflict has been rendered.
        var master_element = null;      // The element where a new course (or alternating courses) started.
        var master_school_unit = null;  // The latest course to be rendered in the timetable.

        var visited = [];

        // Clear existing data from the schedule.
        TimeTabler.clearSchedule();

        for (i = 0; i < DataLoader.timingMap.length; i++) {

            var time_slot= DataLoader.timingMap[i];
            var course_set = TimeTabler.runningSchedule[time_slot];

            if (!course_set || course_set.length === 0) {

                last_conflict_array = null;
                conflict_element = null;
                master_element = null;
                master_school_unit = null;

                continue;
            }

            var term_data = /^term([12])_([a-z]+)_/.exec(time_slot);

            var term = parseInt(term_data[1]);
            var day = term_data[2];

            term_hours[term - 1] += 0.5;

            // Check to see if any Saturday columns are necessary and build them if so.
            if (day === 'sa') {
                if (!saturday_built[0] && term === 1) {
                    TimeTabler.buildSaturday(1);
                    saturday_built[0] = true;
                } else if (!saturday_built[1] && term === 2) {
                    TimeTabler.buildSaturday(2);
                    saturday_built[1] = true;
                }
            }

            // Check if the current course is new. If so, add it's units.
            for (j = 0; j < course_set.length; j++) {

                var newData = true;

                for (k = 0; k < visited.length; k++) {
                    if (TimeTabler.sameCourse(course_set[j], visited[k])) {
                        newData = false;
                        break;
                    }
                }

                if (newData) {
                    visited.push(course_set[j]);
                    var units = course_set[j].credits;

                    if (!course_set[j].termThree) {
                        term_units[0] += units/2;
                        term_units[1] += units/2;
                    } else {
                        term_units[term - 1] += units;
                    }
                }
            }

            // If one school unit is currently occupying the time slot.
            if (course_set.length === 1) {

                conflict_element = null;
                last_conflict_array = null;

                if (!master_element || !TimeTabler.schoolUnitsEqual(course_set[0], master_school_unit)) {

                    master_element = $("#" + time_slot);
                    master_school_unit = course_set[0];

                    setColor = TimeTabler.getColor(master_school_unit);

                    master_element.attr({"bgcolor" : setColor, "rowspan" : 1});
                    master_element.css("background", setColor);

                    supervisor = "<font color='blue'>";

                    if (master_school_unit.targetType === "core" && master_school_unit.target.supervisors.length > 0) {
                        supervisor +=  "</br>" + master_school_unit.target.supervisors[0];
                    }

                    supervisor += "</font>";

                    master_element.html(
                      master_school_unit.code + " " +
                      university.school_unit_prefixes[master_school_unit.targetType] +
                      master_school_unit.target.name + supervisor +
                      (master_school_unit.target.serial ? "</br>" + master_school_unit.target.serial : "") +
                      (master_school_unit.target.loc ? ("</br>" + master_school_unit.target.loc) : "") +
                      (master_school_unit.target.alternating ? "</br><font color='red'>ALTERNATING</font>" : "")
                    );
                } else {
                    master_element.attr("rowspan", parseInt(master_element.attr("rowspan")) + 1);
                    $("#" + time_slot).remove();
                }
            } else {

                // If more than one school unit is currently occupying the time slot.
                master_element = null;
                master_school_unit = null;

                var same_conflict = true;

                if (last_conflict_array) {
                    if (course_set.length === last_conflict_array.length) {
                        for (j = 0; j < course_set.length; j++) {

                            var found = false;

                            for (k = 0; k < last_conflict_array.length; k++) {
                                if (TimeTabler.schoolUnitsEqual(course_set[j], last_conflict_array[k])){
                                    found = true;
                                    break;
                                }
                            }

                            if (!found) {
                                same_conflict = false;
                                break;
                            }
                        }
                    } else {
                        same_conflict = false;
                    }
                } else {
                    same_conflict = false;
                }

                if (same_conflict) {
                    conflict_element.attr("rowspan", parseInt(conflict_element.attr("rowspan")) + 1);
                    $("#" + time_slot).remove();
                } else {
                    conflict_element = $("#" + time_slot);
                    last_conflict_array = course_set;

                    var true_conflict = false;

                    // Check if it's actually a conflict, or just two EOW conflicts.
                    for (j = 0; j < course_set.length; j++) {
                        for (k = 0; k < course_set.length; k++) {
                            if (j != k) {
                                if (!course_set[j].target.alternating ||
                                    !course_set[k].target.alternating ||
                                    !TimeTabler.sameCourse(course_set[j], course_set[k]) ||
                                    course_set[k].targetType === course_set[j].targetType)
                                {
                                    true_conflict = true;
                                    break;
                                }
                            }
                            if (true_conflict) {
                                break;
                            }
                        }
                    }

                    if (true_conflict) {
                        $("#" + time_slot).attr("bgcolor", conflict_color);
                        $("#" + time_slot).css("background", conflict_color);
                    } else {

                        setColor = TimeTabler.getColor(course_set[0]);

                        $("#" + time_slot).attr("bgcolor", setColor);
                        $("#" + time_slot).css("background", setColor);
                    }

                    $("#" + time_slot).attr("rowspan", 1);

                    conflict_element.html("");

                    for (j = 0; j < course_set.length; j++) {

                        var unit = course_set[j];
                        supervisor = "<font color='blue'>";

                        if (unit.targetType === "core") {
                            if (unit.target.supervisors.length > 0) {
                                supervisor +=  "</br>" + unit.target.supervisors[0];
                            }
                        }

                        supervisor += "</font>";

                        conflict_element.append(
                          unit.code + " " +
                          university.school_unit_prefixes[unit.targetType] +
                          unit.target.name + supervisor +
                          (unit.target.serial ? "</br>" + unit.target.serial : "") +
                          (unit.target.loc ? "</br>" + unit.target.loc : "") +
                          "</br><font color='red'>" + (true_conflict ? "*** CONFLICT ***" : "ALTERNATING") +
                          "</br>");
                    }
                }
            }
        }

        $("#term1_details").html("TOTAL HOURS : " + term_hours[0] + ", TOTAL UNITS : " + term_units[0]);
        $("#term2_details").html("TOTAL HOURS : " + term_hours[1] + ", TOTAL UNITS : " + term_units[1]);
    }
};

var BoxManager = {

    activeSelectors : [],

    id_number : 0,

    data_path : {},

    reconstructStage1 : function(state) {

        // Remove the old content.
        $("#courses_div").html("");
        TimeTabler.runningSchedule = {};
        BoxManager.id_number = 0;

        if (!state.selectors || !state.school ||
            !(state.school in DataLoader.universities)){
            alert("Bad scheduling data. Contact the administrator to fix this.");
            window.location = window.location;
        }

        $('input[name=course_color_group]')
            .filter('[value=' + state.type + ']').attr('checked', true);

        // Dump the color-wheel
        for (var color in TimeTabler.colorWheel) {
            TimeTabler.colorWheel[color] = null;
        }

        DataLoader.reloadData = state;
        DataLoader.setUniversity();
    },

    reconstructStage2 : function(state) {

        var prefixes = ["dep_select_", "course_select_", "core_select_",
                        "tutorial_select_", "lab_select_"];

        for (var i = 0; i < state.selectors.length; i++) {

            var selector = state.selectors[i];

            BoxManager.addNewSelector(TimeTabler.masterCourseList);

            for (var y = 0; y < selector.length; y++) {

                if (!selector[y] && y > 2) {
                    continue;
                }

                if ($("#" + prefixes[y] + i + " option[value='" + selector[y] + "']").length > 0) {
                    $("#" + prefixes[y] + i ).val(selector[y]);
                    if (y < 2) {
                        $("#advance_button_" + i).click();
                    }
                } else {
                    alert("Cannot find an option with the value '" +
                          selector[y] + "' under this menu. Skipping");
                    break;
                }
            }

            $("#advance_button_" + i).click();
        }
    },

    removeSelector : function(id) {

        var ans = confirm("Are you sure you want to remove this set of selectors and all associated courses?");

        if (!ans) {
            return;
        }

        var isSet = $('#advance_button_' + id).attr("onclick").indexOf("BoxManager.addCourse(") > -1;

        if (isSet) {
            $('#reverse_button_' + id).click();
        }

        $('#select_set_' + id).remove();

        for (var i = BoxManager.activeSelectors.length; i >= 0; i--) {
            if (BoxManager.activeSelectors[i] === id) {
                BoxManager.activeSelectors.splice(i, 1);
                break;
            }
        }
    },

    addNewSelector : function() {

        var i = 0;

        var id_number = BoxManager.id_number;

        var parentDiv = $("#courses_div");

        var holster = $("<div></div>").attr('id', "select_set_" + id_number);

        var dep_select = $('<select></select>').attr('id', "dep_select_" + id_number);

        // Default value
        dep_select.append(
            $('<option></option>')
              .html('Select a department...')
              .attr({'selected' : 'selected', 'value' : 'bad'})
        );

        var departments = [];

        for (i in TimeTabler.masterDepartmentList) {
            departments.push(i);
        }

        departments.sort();

        for (i = 0; i < departments.length; i++) {
            dep_select.append(
                $('<option></option>')
                  .html(departments[i])
                  .attr("value", TimeTabler.masterDepartmentList[departments[i]])
            );
        }

        var course_select = $('<select></select>').attr({ "disabled": true, "id": "course_select_" + id_number});
        var core_select = $('<select></select>').attr({ "disabled": true, "id" : "core_select_" + id_number});
        var tutorial_select = $('<select></select>').attr({ "disabled": true, "id" : "tutorial_select_" + id_number});
        var lab_select = $('<select></select>').attr({ "disabled": true, "id" : "lab_select_" + id_number});

        var advance_button = $('<button></button>').html("Next").attr({"id" : "advance_button_" + id_number, "onclick" : "BoxManager.setCourse("  + id_number + ")"});
        var reverse_button = $('<button></button>').html("Undo").attr({"disabled" : true, "id" : "reverse_button_" + id_number, "onclick" : "BoxManager.setCourse("  + id_number + ")"});
        var remove_button = $('<button></button>').html("Remove").attr({"id" : "remove_button_" + id_number, "onclick" : "BoxManager.removeSelector("  + id_number + ")"});

        holster.append(dep_select);
        holster.append(course_select);
        holster.append(core_select);
        holster.append(tutorial_select);
        holster.append(lab_select);
        holster.append(advance_button);
        holster.append(reverse_button);
        holster.append(remove_button);

        parentDiv.append(holster);

        BoxManager.data_path[id_number] = [];

        BoxManager.activeSelectors.push(id_number);

        BoxManager.id_number++;
    },

    getData : function(id) {

        var data_pos = TimeTabler.masterCourseList;

        for (var x in BoxManager.data_path[id]) {
            data_pos = data_pos[BoxManager.data_path[id][x]];
        }
        return data_pos;
    },

    returnFromCourse : function(set_number) {

        $("#dep_select_" + set_number).attr("disabled", false);
        $("#course_select_" + set_number).attr("disabled", true);

        $("#course_select_" + set_number).html("");

        $('#advance_button_' + set_number).attr("onclick", "BoxManager.setCourse("  + set_number + ")");
        $('#reverse_button_' + set_number).attr({"onclick" : "", "disabled" : true});

        BoxManager.data_path[set_number].pop();
    },

    setCourse : function(set_number) {

        // Get selected department
        var selectedOption = $("#dep_select_" + set_number + " option:selected").attr('value');

        if (selectedOption === 'bad') {
            alert("Please select a department before proceeding...");
            return;
        }

        BoxManager.data_path[set_number].push(selectedOption);

        $("#dep_select_" + set_number).attr("disabled", true);
        $("#course_select_" + set_number).attr("disabled", false);

        var dataInScope = BoxManager.getData(set_number);

        // For array position lookup
        var courseListings = {};
        var courseNames = [];

        for (var i in dataInScope) {

            var courseInfo = dataInScope[i];
            var name = courseInfo.code + (!courseInfo.name ? "" : " " + courseInfo.name);

            name += (university.show_term)? " T" + courseInfo.term : "";

            courseNames.push(name);
            courseListings[name] = i;
        }

        courseNames.sort();

        // Default value
        $("#course_select_" + set_number).html("");
        $("#course_select_" + set_number).append($('<option></option>')
            .html('Select a course...')
            .attr({'selected' : 'selected', 'value' : 'bad'}));

        for (var x = 0; x < courseNames.length; x++) {
            $("#course_select_" + set_number).append($('<option></option>')
                .html(courseNames[x])
                .attr('value', courseListings[courseNames[x]]));
        }

        $('#advance_button_' + set_number)
            .attr("onclick", "BoxManager.setSchoolUnits("  + set_number + ")");
        $('#reverse_button_' + set_number)
            .attr({"onclick" : "BoxManager.returnFromCourse("  + set_number + ")", "disabled" : false});
    },

    returnFromSchoolUnits: function(set_number) {

        $("#course_select_" + set_number).attr("disabled", false);
        $("#core_select_" + set_number).attr("disabled", true);
        $("#lab_select_" + set_number).attr("disabled", true);
        $("#tutorial_select_" + set_number).attr("disabled", true);

        $("#core_select_" + set_number).html("");
        $("#lab_select_" + set_number).html("");
        $("#tutorial_select_" + set_number).html("");

        $('#advance_button_' + set_number).attr("onclick", "BoxManager.setSchoolUnits("  + set_number + ")");
        $('#reverse_button_' + set_number).attr("onclick", "BoxManager.returnFromCourse("  + set_number + ")");

        BoxManager.data_path[set_number].pop();
    },

    setSchoolUnits: function(set_number) {

        // Get selected department
        var selectedOption = $("#course_select_" + set_number + " option:selected").attr('value');

        if (selectedOption === 'bad') {
            alert("Please select a course before proceeding...");
            return;
        }

        BoxManager.data_path[set_number].push(selectedOption);

        $("#course_select_" + set_number).attr("disabled", true);

        var dataInScope = BoxManager.getData(set_number);

        // Default value
        $("#su_select_" + set_number)
            .append($('<option></option>')
                        .html('Select a section type...')
                        .attr({'selected' : 'selected', 'value' : 'bad'}));

        // Load each data set.
        for (var section_type in dataInScope) {

            if (section_type !== "core" && section_type !== "lab" &&
                section_type !== "tutorial"){
                continue;
            }

            var courseInfo = dataInScope[section_type];

            var prefix = university.school_unit_prefixes[section_type];
            var el_name = university.school_unit_names[section_type].toLowerCase();

            $("#" + section_type + "_select_" + set_number).html("");
            $("#" + section_type + "_select_" + set_number)
                .attr("disabled", false);
            $("#" + section_type + "_select_" + set_number)
                .append($('<option></option>')
                            .html('Select a ' + el_name + '...')
                            .attr({'selected' : 'selected', 'value' : 'bad'}));

            var secListings = {};
            var secNames = [];

            for (section in dataInScope[section_type]) {
                var name = dataInScope[section_type][section].name;
                secNames.push(name);
                secListings[name] = section;
            }

            secNames.sort(
                function (a,b) {
                    return a - b;
                }
            );

            for (var i in secNames) {
                $("#" + section_type + "_select_" + set_number)
                    .append($('<option></option>')
                                .html(prefix + secNames[i].toString())
                                .attr('value', secListings[secNames[i]]));
            }
        }

        $('#advance_button_' + set_number)
            .attr("onclick", "BoxManager.addCourse("  + set_number + ")");
        $('#reverse_button_' + set_number)
            .attr(
                "onclick",
                "BoxManager.returnFromSchoolUnits("  + set_number + ")"
            );
    },

    addCourse : function(set_number) {

        var i = 0;

        // Get selected sections.
        var selectedCore = $("#core_select_" + set_number + " option:selected").attr('value');
        var selectedLab = $("#lab_select_" + set_number + " option:selected").attr('value');
        var selectedTut = $("#tutorial_select_" + set_number + " option:selected").attr('value');

        var needCore = typeof selectedCore != 'undefined';
        var needLab = typeof selectedLab != 'undefined';
        var needTut = typeof selectedTut != 'undefined';

        // Check to ensure all required data has been entered.
        var needSet = [needCore, needLab, needTut];
        var selectedSet = [selectedCore, selectedLab, selectedTut];
        var schoolUnitNameIdentifier = ['core', 'lab', 'tutorial'];

        for (i = 0; i < 3; i++) {
            if (needSet[i] && selectedSet[i] === 'bad') {
                alert("Please select a " +
                    university.school_unit_names[schoolUnitNameIdentifier[i]].toLowerCase() +
                    " before proceeding..."
                );
                return;
            }
        }

        var schoolUnitTypes = ['core', 'lab', 'tutorial'];

        for (i = 0; i < 3; i++) {
            if (needSet[i]) {

                BoxManager.data_path[set_number].push(schoolUnitTypes[i]);
                BoxManager.data_path[set_number].push(selectedSet[i]);

                var targetPayload = BoxManager.createTarget(set_number);

                TimeTabler.importSchoolUnit(targetPayload);

                BoxManager.data_path[set_number].pop();
                BoxManager.data_path[set_number].pop();
            }
        }

        TimeTabler.renderSchedule();

        $('#core_select_' + set_number).attr("disabled", true);
        $('#lab_select_' + set_number).attr("disabled", true);
        $('#tutorial_select_' + set_number).attr("disabled", true);

        $('#advance_button_' + set_number).attr("disabled", true);
        $('#reverse_button_' + set_number).attr("disabled", false);

        $('#reverse_button_' + set_number)
            .attr("onclick", "BoxManager.removeCourse(" + set_number + ")");
    },

    removeCourse : function(set_number) {

        var i = 0;

        // Get selecteds sections.
        var selectedCore = $("#core_select_" + set_number + " option:selected").attr('value');
        var selectedLab = $("#lab_select_" + set_number + " option:selected").attr('value');
        var selectedTut = $("#tutorial_select_" + set_number + " option:selected").attr('value');

        var needCore = typeof selectedCore != 'undefined';
        var needLab = typeof selectedLab != 'undefined';
        var needTut = typeof selectedTut != 'undefined';

        var sectioningData = [
            {'enabled' : needCore, 'type' : 'core', 'choice' : selectedCore},
            {'enabled' : needLab, 'type' : 'lab','choice' : selectedLab},
            {'enabled' : needTut, 'type' : 'tutorial', 'choice' : selectedTut}
        ];

        for (i = 0; i < sectioningData.length; i++) {
            if (sectioningData[i].enabled) {

                BoxManager.data_path[set_number].push(sectioningData[i].type);
                BoxManager.data_path[set_number].push(sectioningData[i].choice);

                var targetPayload = BoxManager.createTarget(set_number);

                TimeTabler.removeSchoolUnit(targetPayload);

                BoxManager.data_path[set_number].pop();
                BoxManager.data_path[set_number].pop();
            }
        }

        TimeTabler.renderSchedule();

        $('#core_select_' + set_number).attr("disabled", !needCore);
        $('#lab_select_' + set_number).attr("disabled", !needLab);
        $('#tutorial_select_' + set_number).attr("disabled", !needTut);

        $('#advance_button_' + set_number).attr("disabled", false);
        $('#reverse_button_' + set_number).attr("disabled", false);

        $('#reverse_button_' + set_number)
            .attr("onclick", "BoxManager.returnFromSchoolUnits(" + set_number + ")");
    },

    createTarget : function(set_number) {

        var dpath = BoxManager.data_path[set_number];
        var dep = dpath[0];

        var dataTarget = TimeTabler.masterCourseList[dpath[0]][dpath[1]];

        var targetPayload = jQuery.extend(true, {}, dataTarget);

        targetPayload.target = dataTarget[dpath[2]][dpath[3]];
        targetPayload.targetType = dpath[2].toLowerCase();
        targetPayload.dep = dpath[0];

        return targetPayload;
    }
};
