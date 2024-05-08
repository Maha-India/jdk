/*
 * Copyright (c) 2024, Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2024, Red Hat Inc. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 *
 */

#ifndef SHARE_NMT_VMATREE_HPP
#define SHARE_NMT_VMATREE_HPP

#include "memory/resourceArea.hpp"
#include "nmt/nmtNativeCallStackStorage.hpp"
#include "nmt/nmtTreap.hpp"
#include "runtime/os.hpp"
#include "utilities/globalDefinitions.hpp"
#include "utilities/growableArray.hpp"

// A VMATree stores a sequence of points on the natural number line.
// Each of these points stores information about a state change.
// For example, the state may go from released memory to committed memory,
// or from committed memory of a certain MEMFLAGS to committed memory of a different MEMFLAGS.
// The set of points is stored in a balanced binary tree for efficient querying and updating.
class VMATree {

  // A position in memory.
  using position = size_t;

  class AddressComparator {
  public:
    static int cmp(position a, position b) {
      if (a < b) return -1;
      if (a == b) return 0;
      if (a > b) return 1;
      ShouldNotReachHere();
    }
  };

public:
  enum class StateType : uint8_t { Reserved, Committed, Released };

  // Each point has some stack and a flag associated with it.
  struct Metadata {
    const NativeCallStackStorage::StackIndex stack_idx;
    const MEMFLAGS flag;

    Metadata() : stack_idx(), flag(mtNone) {}

    Metadata(NativeCallStackStorage::StackIndex stack_idx, MEMFLAGS flag)
    : stack_idx(stack_idx), flag(flag) {}

    static bool equals(const Metadata& a, const Metadata& b) {
      return a.flag == b.flag &&
             NativeCallStackStorage::StackIndex::equals(a.stack_idx, b.stack_idx);
    }
  };

  struct IntervalState {
  private:
    // Store the type and flag as two bytes
    uint8_t type_flag[2];
    NativeCallStackStorage::StackIndex sidx;

  public:
    IntervalState() : type_flag{0,0}, sidx() {}
    IntervalState(StateType type, Metadata data) {
      type_flag[0] = static_cast<uint8_t>(type);
      type_flag[1] = static_cast<uint8_t>(data.flag);
      sidx = data.stack_idx;
    }

    StateType type() const {
      return static_cast<StateType>(type_flag[0]);
    }

    MEMFLAGS flag() const {
      return static_cast<MEMFLAGS>(type_flag[1]);
    }

    Metadata metadata() const {
      return Metadata{sidx, flag()};
    }

    const NativeCallStackStorage::StackIndex stack() const {
     return sidx;
    }
  };

  // An IntervalChange indicates a change in state between two intervals. The incoming state
  // is denoted by in, and the outgoing state is denoted by out.
  struct IntervalChange {
    IntervalState in;
    IntervalState out;

    bool is_noop() {
      return (in.type() == StateType::Released && out.type() == StateType::Released) ||
             (in.type() == out.type() && Metadata::equals(in.metadata(), out.metadata()));
    }
  };

  using VMATreap = TreapCHeap<position, IntervalChange, AddressComparator>;
  using TreapNode = VMATreap::TreapNode;

private:
  VMATreap _tree;
public:
  VMATree() : _tree() {}

  struct SingleDiff {
    int64_t reserve;
    int64_t commit;
  };
  struct SummaryDiff {
    SingleDiff flag[mt_number_of_types];
    SummaryDiff() {
      for (int i = 0; i < mt_number_of_types; i++) {
        flag[i] = SingleDiff{0, 0};
      }
    }
  };

  SummaryDiff register_mapping(position A, position B, StateType state, Metadata& metadata);

  SummaryDiff reserve_mapping(position from, position sz, Metadata& metadata) {
    return register_mapping(from, from + sz, StateType::Reserved, metadata);
  }

  SummaryDiff commit_mapping(position from, position sz, Metadata& metadata) {
    return register_mapping(from, from + sz, StateType::Committed, metadata);
  }

  SummaryDiff release_mapping(position from, position sz) {
    Metadata empty;
    return register_mapping(from, from + sz, StateType::Released, empty);
  }

public:
  template<typename F>
  void visit_in_order(F f) const {
    _tree.visit_in_order(f);
  }
};

#endif
