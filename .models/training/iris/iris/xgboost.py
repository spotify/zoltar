#!/usr/bin/env python
# -*- coding: utf-8 -*-
#
# Copyright 2018 Spotify AB.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#

from __future__ import absolute_import

import logging
from collections import defaultdict
from os.path import join as pjoin

import numpy as np
import tensorflow as tf
import xgboost as xgb
from spotify_tensorflow.dataset import Datasets
from tensorflow.python.lib.io import file_io

logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)

flags = tf.app.flags
FLAGS = flags.FLAGS
flags.DEFINE_integer('rounds', 50, 'Number of Rounds')
flags.DEFINE_string('local_dir', "/tmp/features", 'GCS Train Path')


def transform_dataset(ctx, dataset):
  (feature_names, label_names) = ctx.multispec_feature_groups
  data = defaultdict(list)

  for key, values in dataset.iteritems():
    if key in feature_names:
      data["features"].append(values)
    else:
      data["labels"].append(values)

  return zip(*data["features"]), np.argmax(zip(*data["labels"]), axis=1)


def train(_):
  training_dir = pjoin(FLAGS.training_set, FLAGS.train_subdir)
  feature_context = Datasets.get_context(training_dir)
  
  (feature_names, label_names) = feature_context.multispec_feature_groups

  training_dataset = Datasets.dict.read_dataset(training_dir)
  (feature_train_data, labels_train_data) = transform_dataset(feature_context,
                                                              training_dataset)

  params = {
    'objective': 'multi:softprob',
    'verbose': False,
    'num_class': len(label_names),
    'max_depth': 6,
    'nthread': 4,
    'silent': 1
  }

  xg_train = xgb.DMatrix(feature_train_data,
                         label=labels_train_data)
  xg_model = xgb.train(params, xg_train, FLAGS.rounds)

  model_path = pjoin(FLAGS.local_dir, "iterator.model")
  xg_model.save_model(model_path)

  output_path = pjoin(FLAGS.training_set, "xgboost/iterator.model")
  file_io.copy(model_path, output_path, overwrite=True)


def main():
  tf.app.run(main=train)


if __name__ == "__main__":
  main()
