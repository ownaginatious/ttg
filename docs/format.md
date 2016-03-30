# Data Model Overview

The purpose of this document is to provide an overview of the universal timetable model to be used in the new revision of *The Quintessential Timetable Generator*. This new data model is a significant improvement over that currently in use as it provides significantly more flexibility in supporting schools which *do not* follow a structure similar to McMaster University.

This introduction will be given in a top-down approach, starting with what defines the root of this data store, schools, and gradually fixating on specifics such the the definition of enrollment data. etc

## Diagram Notation

The diagrams that will follow are written in a notation that resembles *[JavaScript Object Notation (JSON)](http://en.wikipedia.org/wiki/JSON)*, but adds additional features to help break down the content into more concise diagrams for easier understanding of structuring and data types.

### Type Notation

Typically in first order logic, the type of a variable is expressed using the *colon* operator `:`

For example if we wanted to say some variable *X* is of type *Integer*, we would say `X : Integer` or `x ∈ Integer`, where ∈ in this context is shorthand to mean "an element of the set of all integers". Because JSON already uses this syntax to mean “has the value”, rather than, “has the type”, type definition will be expressed using left and right chevrons (`<>`) instead.

This would change the previous example of `X : Integer` to now be expressed as `X : <Integer>`. This is not an exact 1-to-1 correspondence, and means something more along the lines of *the key X points to a value, which is of type integer.* Nevertheless, this is sufficient for the purpose of this document.

### Set notation

Set notation is used for defining types that are *enumerated*. An enumerated type is a countable set. The possible values for variables that are of the type of some enumeration are only the members of that enumeration.

For example, consider a set `Animals` with the contents `{ Dog, Cat, Mouse, Rhino }`. Let's say `X` is a variable of type `Animal`. This is written formally as, `X ∈ { Dog, Cat, Mouse, Rhino }` or in shorthand as `X ∈ Animals`. Because we are using the chevron-based type expression as mentioned in the previous subsection, we would express this as, `X : <Y ∈ Animals>`. This means, *the key X points to the value Y, which is an element of the Animals enumeration.* Arguably, `X : <Animals>` seems like it should be sufficient too, but we want to put emphasis on the fact that the type of the data is an enumeration.

### Slash notation

This is a made-up non-standard notation (looks like this `“/some_string/"`) used to designate that the value within quotes is **not** the value that that key will have, but is a description of the type of keys that will be there. It is simply a place holder used to designate that the value is a unique key within some mapping to some data.

## Data Model

### The root<a name="root-head"></a>

The root is the top-most container where *all* timetable data for *all* universities is stored as well as generic term information. This would act as the root of a NoSQL database used for horizontally scaled distribution.

```python
{
    "terms" : {
        ...
        "/language_code/" : {
            ...
            <Term> : <String>
            ...
        }
        ...
    },
    "schools" : {
        ...
        "/school_key/" : <School>
        ...
    }
}
```
#### terms `Map of language code to Terms mapped to strings`

A mapping of language codes to mappings of `Term` keys to strings. The purpose of this datatype is to map each value of the `Term`  enumeration to a string representing that value in a human understandable form for a specific language.

The [`Term`](#term-head) enumeration is integer based and somewhat unclear in its natural form as to what each element means. This mapping of mappings is intended not only potentially for display purposes, but also as a reference point for developers.

Read more on the [`Term`](#term-head) datatype below for more information.

#### schools `Map of school keys to schools`

A mapping from the unique key for each supported school to its respective [`School`](#school-head) data structure. An example key is `mcmaster` for *McMaster University*.

### Term <a name="term-head"></a>

Due to the vast complexity with which some universities do scheduling, there are a large number of potential terms for which courses can be scheduled.  Most courses at universities are scheduled within *semestered* terms: fall, spring or summer (either the first/second half or full summer). Some universities, such as the *University of Western Ontario*, also schedule courses within only *half* of what most consider the regular term, which is known as a *quarter*.

The `Term` structure only exists abstractly within the API. Its type is restricted to this set of integers with the following definitions. In more type strict languages, this would be represented as an *enumeration* if possible. The following demonstrates a mapping of terms from their enumerated element values to standard American English. See the `terms` key of [`The root`](#root-head) structure listed above.

```javascript
{
    "en_US" : {
           "0" : "Fall first quarter", // ~September through mid-October
            "1" : "Fall second quarter", // ~mid-October through December
            "2" : "Fall semester", // ~September through December
            "3" : "Spring first quarter", // ~January through mid-February
            "4" : "Spring second quarter", // ~mid-February through April
            "5" : "Spring semester", // ~January through April
            "6" : "Full school year (Fall and spring semesters)", // ~September through April
            "7" : "Summer first semester first quarter", // ~through May
            "8" : "Summer first semester second quarter", // ~through June
            "9" : "Summer first semester", // ~May through June
            "10" : "Summer second semester first quarter", // ~through July
            "11" : "Summer second semester second quarter", // ~through August
            "12" : "Summer second semester", // ~July through August
            "13" :  "Full summer", // ~May through August
            "14" :  "Full year", // ~January through December
            "15" :  "Not offered",
            "16" :  "Unscheduled"
        }
}
```
An *unscheduled* term means that whatever period marked it occurs outside of the regular scheduling provided by the university. Periods of this type should be ignored and hidden for timetable scheduling purposes. The difference between a *not offered* period and a *cancelled* period is that a *cancelled* period was supposed to be scheduled, but was not due to some restriction not being met (i.e. not enough enrollment), while *not offered* implies that object was never to be scheduled within this term for some reason. This information is superfluous for scheduling purposes and should be ignored in the same fashion as *unscheduled* objects. It is recorded for potentially informing users a course they are looking for is unavailable.

The timeframes in the comments are given as a general guideline as to what each time period represents. The definitions can be changed from school to school.

### School<a id="school-head"></a>

The `School` data type defines a university tracked by the timetable generator. It tracks all present and historical data about a university's scheduling and holds properties defining the scheduling of that university. It is assumed these properties remain static over time.

Each university has slightly different notational aspects for displaying their scheduling information. Some universities also encode information not provided by others, such as course serial numbers (used by *The University of Waterloo* and *The University of Western Ontario*) for course and section registration, enrollment data and waiting lists.

```python
{
    "name": <String>,
    "departments" : {
        ...
        "/department_id/" : <String>,
        ...
    },
    "displays_dep_prefix" : <Boolean>,
    "section_types": {
        ...
        "/section_type_id/" : <String>,
        ...
    },
    "timetables" : {
        ...
        "/year/" : {
            "/<Y ∈ Term>/" : <Timetable>
        }
        ...
    }
}
```

#### name `String`

The name of the university used for display purposes. Example: *McMaster University*, *The University of Western Ontario*, .etc

#### departments `Map of department keys to strings`

Used for describing full department names based on unique identifiers. These unique identifiers may be used for display purposes depending on the school.

For example, a possible key-value pair for *McMaster University* would be `"SFWR ENG" : "Software Engineering"`.

#### displays_dep_prefix `Boolean`

Some universities tend to concatenate department headers with their course codes (e.g. at *McMaster University*, *SFWR ENG 3F03*, where *SFWR ENG* is the department and *3F03* is the course code). If that should be done for this particular university, then this value is `true`;  `false` otherwise.

#### section_types `Map of section keys to strings`

This variable is used for providing a displayable full-name for a section type. The key is the shorthand, used for internal purposes and typically also as a prefix for when displaying course sections within the timetable.

For example, a possible key-value pair for McMaster University would be `C : Core`. `C` is used as a unique identifier for grouping all lecture core types at McMaster University and also as a prefix. `Core` is used only for descriptive display purposes.

#### timetables `Map of year keys to terms to timetables`

Historical data is retained for all recorded terms after support for a school is implemented. Depending on the way the school releases scheduling information, a year key could point to several term keys.

A typical example of a year of Scheduling at *McMaster University* would have two terms: full summer (13) and full school year (6) for a single year where the latter represents the standard 2015 to 2016 school year.

```python
{
    "2015" : {
        "6" : <Timetable>,
        "13" : <Timetable>
    }
}
```

### Timetable

The `Timetable` represents a set of scheduling data for a particular point in time; a year and term pairing. This is the first temporaly significant structure that appears under a school.

```python
{
    "last_update" : <String>,
    "courses" : {
        ...
        "/course_id/" : <Course>
        ...
    }
}
```

#### last_update `String`

The date and time (UTC) that the timetable data was last harvested. The format is `YYYY-MM-DDThh:mmZ`. The `T` is the delimiter indicating the beginning of time information in the [ISO 8601](http://en.wikipedia.org/wiki/ISO_8601) time standard and the *Z* indicates that the timezone is UTC. This value is used for documenting the *freshness* of the data each time it is parsed and updated.

### courses `Map of course keys to courses`

A mapping for the courses available for this timetable. The key is a unique ID for identifying the course among others. It should not be used for any sort of display purposes. Course keys are guaranteed to be unique for the particular course they describe among all courses at that particular university.

### Course

A course is the most important schedulable unit that any timetable scheduling implementation should work with. Every course at a university, regardless of whether it is offered or not, has an associated course data structure somewhere in the timetable.

```python
{
    "name" : <String>,
    "department" : <String>,
    "code" : <String>,
    "credits" : <Decimal>,
    "description": <String>,
    "corequisites": [ ... <String> ...],
    "cross_listings": [ ... <String> ...],
    "prerequisites": [ ... <String> ...],
    "antirequisites": [ ... <String> ...],
    "notes": [ ... <String> ...],
    "term" : <Y ∈ Term>,
    "sections" : {
        ...
        "/section_type_id/" : {
            ...
            "/section_id/" : <Section>
            ...
        }
        ...
    }
}
```

#### name `String`

The name of the course (e.g. *Introduction to Linguistics 1*).

#### department `String`

The unique ID of the department that the course belongs to (e.g. *SFWR ENG*).

#### code `String`

The course code of the course (e.g. *1A03, 2DA4. etc*).

#### credits `Decimal`

The number of credits that the course is worth (e.g. *3.0 credits for LING 1A03*).

#### description `String`

A description of the course, if any exists. (e.g. *Ordinary differential equations, Laplace transforms, eigenvalues and eigenvectors, applications.*)

#### corequisites `[ ... String ... ]`

The unique identifying codes of any courses for which one must be simultaneously registered for this one can be taken.

#### cross_listings `[ ... String ... ]`

The unique identifying codes of any courses that are this course, but crosslisted under another department or code. These courses, although likely identical, are stored redundantly to reduce data and parsing complexity in the scrapers.

#### prerequisites `[ ... String ... ]`

The unique identifying codes of any courses that must have been completed before this one can be taken (e.g. *Engineering Math I is a prerequisite of Engineering Math II*).

#### antirequisites `[ ... String ... ]`

The unique identifying codes of any courses that cannot have already been completed or in progress for this one to be taken (e.g. *Engineering Physics I is an anti-requisite of Physics I*).

#### notes `[ ... String ... ]`

Any relevant notes as to this course. Courses may or may not have these depending on the school they come from.

#### term <Term>

The `Term` that this course occurs in. This key will always be set to a term that is covered by the timetable's overall term. For example, if the timetable's term value is set to `6` (full school year), the the only possible values for the term variable within this structure are the those that are within fall, spring or are unscheduled, cancelled or not offered. Refer to the [`Term`](#term-head) section for more information.

#### sections `Map of section types keys to section types`

Courses contain a number of *section types* and a number of *sections* that fall under each of those types (i.e. sections *C01*,* C02* falling under *C*, which signifies lectures for *McMaster University*). The *section type* keys found here are a subset of those listed under the `section_types` variable in the top-level [`School`](#school-head) data structure.

### Section

The section contains information about an attendable component of a course, which aggregates several time periods intended to be attended together.

```python
{
    "serial" : <String>,
    "online" : <Boolean>,
    "max_enrolled" : <Integer>,
    "num_enrolled" : <Integer>,
    "section_full" : <Boolean>,
    "max_waiting" : <Integer>,
    "num_waiting" : <Integer>,
    "has_waitlist" : <Boolean>,
    "alternating" : <Boolean>,
    "cancelled" : <Boolean>,
    "r_periods" : [ ... <RepeatingPeriod> ... ],
    "s_periods" : [ ... <SinglePeriod> ... ]
}
```

#### Serial `String`

A unique identifier assigned by _some_ schools to _every_ section for registration purposes.

#### online `Boolean`

An indicator of whether the section is an online course, usually indicating it likely does not need to be scheduled. These sections typically **cannot** be scheduled, as most online courses are provided as video lectures that are watched at student leisure. However, online courses can have scheduled periods of time in which students can ask the professor or a TA questions either online or during office hours. This value is not present if unknown.

#### max_enrolled `Integer`

The maximum number of students that can be enrolled in a course. This value is not present if there are no enrollment figures or no specified enrollment cap.

#### num_enrolled `Integer`

The number of students currently enrolled in this section. This value is not present if enrollment figures are not published.

#### max_waiting `Integer`

The maximum number of students who can be waiting on the waiting list to be enrolled in a course. This value is not present if there is no waiting list or no waiting list cap.

#### num_waiting `Integer`

The number of students on the waiting list to be enrolled in this section. This value is not present if waiting lists either do not exist or are not published.

#### alternating `Boolean`

Whether this section *alternates* with other sections of the same course. Some schools, such as *McMaster University*, tend to schedule courses with sections that alternate with each other each week (i.e. week 1 is a *lab*, week 2 is a *tutorial*, week 3 is a *lab* ... etc). Sections within a course that alternate can be scheduled within the same time slot without conflict. This value is not present if alternation is irrelevant or is unknown.

#### cancelled `Boolean`

Marks if a section has been cancelled for some reason (e.g. not enough enrollment). This value is not present if unknown.

#### r_periods `[ ... RepeatingPeriod ... ]`

An array of repeating time periods over which the section is scheduled.  These have no unique identifying information, hence why they are an array rather than an object. Please see [`RepeatingPeriod`](#r-period-head) for more information.

#### s_periods `[ ... SinglePeriod ... ]`

An array of discrete time periods over which the section is scheduled.  These have no unique identifying information, hence why they are an array rather than an object. Please see [`SinglePeriod`](#s-period-head) for more information.

### RepeatingPeriod<a name="s-period-head"></a>

Denotes a repeating unit of time over which some section occurs.

```python
{
    "campus" : <String>,
    "room" : <String>,
    "online" : <Boolean>,
    "day" : <Integer>,
    "start" : <String>,
    "end" : <String>,
    "term" : <Y ∈ Term>,
    "supervisors" : [ ... <String> ... ]
}
```
#### campus `String`

Larger universities sometimes schedule courses over multiple campuses. This value is not present if irrelevent (i.e. the school only has one campus).

#### room `String`

Every period in which a section occurs happens in a room. Originally, this variable was part of the `Course` type, but was moved after it was determined that some sections may have periods that occur in different rooms (e.g. the Tuesday lecture of *PHYS 1E03 *may occur in *ABB-102, *but the Thursday one in* ABB-135*). This is used for display purposes, but could also potentially be used for computing when certain rooms are free.

#### online `Boolean`

Whether or not the period occurs online. This value is not present if unknown.

#### day `Integer`

The day of the week the time period occurs on (1 = Monday through 7 = Sunday).

#### start `String`

An [ISO 8601](http://en.wikipedia.org/wiki/ISO_8601) formatted string of either just time or a date and time. The format of this string is `hh:mm` an example of this being, `16:30` to represent *4:30 PM*. Timezone information is assumed to be inferred by the location of the university.

#### end `String`

See the above definition of `start`. Guaranteed to occur chronologically *after* the date described by the `start` variable without ambiguity. Repeating periods spanning more than a single day are **not** supported.

#### term `Term`

It was discovered that courses that span multiple terms (e.g. a full term 6 unit course at McMaster University) can sometimes change sectional scheduling for different terms (e.g. McMaster *iSci* courses are an example of courses that have this property). Because of this, periods are always scheduled with a term that is covered by that of the course. See the [`Term`](#term-head) section for more examples and the course section about the term variable for more information on this "coverage" property.

#### supervisors `[ ... String ... ]`

Supervisors are instructors assigned to a particular time period. This used to be a component of the *course* variable, but it was discovered that some universities will schedule different instructors for different time periods within the *same* section. This is an array as some universities will assign multiple supervisors or instructors to a time period.

### SinglePeriod<a name="s-period-head"></a>

Originally, the only type of period was the *repeating* period, which most are familiar with: a time period for a section that repeats *weekly* ([`RepeatingPeriod`](#r-period-head)). It was later discovered, however, that some schools also put *non-repeating* information into their timetables (e.g. *The University of Waterloo* will put test dates into their timetables, which include a specific *date*).

```python
{
    "campus" : <String>,
    "room" : <String>,
    "online" : <Boolean>,
    "start" : <String>,
    "end" : <String>,
    "term" : <Y ∈ Term>,
    "supervisors" : [ ... <String> ... ]
}
```
#### campus `String`

The campus where the period occurs. This value is not present if irrelevent (i.e. the school only has one campus).

#### room `String`

The room where the period occurs.

#### online `Boolean`

Whether or not the period occurs online. This value is not present if unknown.

#### start `String`

An [ISO 8601](http://en.wikipedia.org/wiki/ISO_8601) formatted string of either just time or a date and time. The format of this string is `YYYY-MM-DDThh:mm` an example of which being, `2015-05-12T16:30` to represent *4:30 PM on Tuesday, May 12, 2015*. Timezone information is assumed to be inferred by the location of the university.

#### end `String`

See the above definition of `start`. Guaranteed to occur chronologically *after* the date described by the `start` variable without ambiguity.

#### term `Term`

The term that the period occurs in.

#### supervisors `[ ... String ... ]`

Supervisors (or instructors) assigned to this period.