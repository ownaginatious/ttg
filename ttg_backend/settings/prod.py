import os
from .base import *
from django

DEBUG = False
SECRET_KEY = os.environ['SECRET_KEY']

STATIC_ROOT = '../ttg_frontend/dist'
STATIC_URL = '/static/'
