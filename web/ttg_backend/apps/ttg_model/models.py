from django.db import models
from django.utils import timezone
from django.utils.translation import ugettext as _
from model_utils import Choices
from model_utils.fields import MonitorField


class Note(models.Model):
    text = models.TextField(max_length=1000)

    class Meta:
        abstract = True

    def __unicode__(self):
        return unicode(self.text)


class School(models.Model):
    key = models.CharField(max_length=50, unique=True)
    name = models.CharField(max_length=100)
    displays_department_prefix = models.BooleanField(default=False)


class Term(models.Model):
    school = models.ForeignKey(School, on_delete=models.CASCADE,
                               related_name='terms',
                               related_query_name='term')
    code = models.CharField(max_length=20)
    parent_term = models.ForeignKey('self', on_delete=models.CASCADE,
                                    related_name='subterms',
                                    related_query_name='subterm',
                                    null=True)
    short_name = models.CharField(max_length=50, null=True)
    long_name = models.CharField(max_length=100, null=True)

    class Meta:
        unique_together = (('code', 'school'),)

    def __unicode__(self):
        return unicode("%s %s" % (self.school.key, str(self.term)))


class TermInstance(models.Model):

    year = models.IntegerField()
    term = models.ForeignKey(Term, on_delete=models.CASCADE,
                             related_name='instances',
                             related_query_name='instance')
    start_date = models.DateField()
    end_date = models.DateField()

    class Meta:
        unique_together = (('year', 'term'),)

    def __unicode__(self):
        return unicode("[%d] %s" % (self.year, str(self.term)))


class TimeTable(models.Model):

    last_update = models.DateTimeField(default=timezone.now)
    term = models.ForeignKey(TermInstance, on_delete=models.CASCADE,
                             related_name='timetables',
                             related_query_name='timetable')
    departments = models.ManyToManyField('Department')

    def __unicode__(self):
        return unicode("[%s] last update: %s" %
                       (str(self.term), str(self.name)))


class Department (models.Model):

    school = models.ForeignKey(School, on_delete=models.CASCADE,
                               related_name='departments',
                               related_query_name='department')
    name = models.CharField(max_length=100)
    code = models.CharField(max_length=20)

    class Meta:
        unique_together = (('school', 'name', 'code'),)

    def __unicode__(self):
        return unicode("[%s] %s" % (self.code, self.name))


class Course(models.Model):

    name = models.CharField(max_length=100)
    code = models.CharField(max_length=20)
    department = models.ForeignKey(Department, on_delete=models.CASCADE)
    timetable = models.ForeignKey(TimeTable, on_delete=models.CASCADE,
                                  related_name='courses',
                                  related_query_name='course')
    term = models.ForeignKey(Term, on_delete=models.CASCADE)
    credits = models.FloatField()
    description = models.TextField(max_length=1000)
    pre_requisites = models.ManyToManyField('self')
    anti_requisites = models.ManyToManyField('self')
    co_requisites = models.ManyToManyField('self')
    cross_listings = models.ManyToManyField('self')

    def __unicode__(self):
        return unicode("[%s] %s (%s)" %
                       (self.code, self.name, self.department.code))


class CourseNote(Note):
    course = models.ForeignKey(Course, on_delete=models.CASCADE)


class SectionType(models.Model):
    school = models.ForeignKey(School, on_delete=models.CASCADE,
                               related_name='section_types',
                               related_query_name='section_type')
    name = models.CharField(max_length=100)
    code = models.CharField(max_length=20)


class Section(models.Model):

    course = models.ForeignKey(Course, on_delete=models.CASCADE,
                               related_name='courses',
                               related_query_name='course')
    section_type = models.ForeignKey(SectionType,
                                     on_delete=models.CASCADE)
    serial = models.CharField(max_length=20)
    online = models.BooleanField(default=False)
    max_enrolled = models.IntegerField(null=True)
    num_enrolled = models.IntegerField(null=True)
    max_waiting = models.IntegerField(null=True)
    num_waiting = models.IntegerField(null=True)
    alternating = models.BooleanField(default=False)
    cancelled = models.BooleanField(default=False)


class SectionNote(Note):
    course = models.ForeignKey(Section, on_delete=models.CASCADE,
                               related_name='notes',
                               related_query_name='note')


class Period(models.Model):

    DAY = Choices(
            ('MO', 'monday', _('monday')),
            ('TU', 'tuesday', _('tuesday')),
            ('WE', 'wednesday', _('wednesday')),
            ('TH', 'thursday', _('thursday')),
            ('FR', 'friday', _('friday')),
            ('SA', 'saturday', _('saturday')),
            ('SU', 'sunday', _('sunday')),
        )

    campus = models.CharField(max_length=20)
    room = models.CharField(max_length=20)
    online = models.BooleanField(default=False)
    term = models.ForeignKey(Term, on_delete=models.CASCADE)
    start_time = models.TimeField()
    end_time = models.TimeField()

    # Repeating periods only
    day = models.CharField(max_length=2, null=True, choices=DAY)

    # Single periods only
    start_date = models.DateField(null=True)
    end_date = models.DateField(null=True)


class PeriodNote(Note):
    course = models.ForeignKey(Period, on_delete=models.CASCADE,
                               related_name='notes',
                               related_query_name='note')


class Supervisor(Note):
    course = models.ForeignKey(Period, on_delete=models.CASCADE,
                               related_name='supervisors',
                               related_query_name='supervisor')
    first_name = models.CharField(max_length=50)
    last_name = models.CharField(max_length=50)
