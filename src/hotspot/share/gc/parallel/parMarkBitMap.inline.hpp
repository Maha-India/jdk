/*
 * Copyright (c) 2017, 2023, Oracle and/or its affiliates. All rights reserved.
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

#ifndef SHARE_GC_PARALLEL_PARMARKBITMAP_INLINE_HPP
#define SHARE_GC_PARALLEL_PARMARKBITMAP_INLINE_HPP

#include "gc/parallel/parMarkBitMap.hpp"

#include "utilities/align.hpp"
#include "utilities/bitMap.inline.hpp"

inline ParMarkBitMap::ParMarkBitMap():
  _region_start(nullptr), _region_size(0), _beg_bits(), _virtual_space(nullptr), _reserved_byte_size(0)
{ }

inline void ParMarkBitMap::clear_range(idx_t beg, idx_t end) {
  _beg_bits.clear_range(beg, end);
}

inline ParMarkBitMap::idx_t ParMarkBitMap::bits_required(size_t words) {
  return words_to_bits(words);
}

inline ParMarkBitMap::idx_t ParMarkBitMap::bits_required(MemRegion covered_region) {
  return bits_required(covered_region.word_size());
}

inline HeapWord* ParMarkBitMap::region_start() const {
  return _region_start;
}

inline HeapWord* ParMarkBitMap::region_end() const {
  return region_start() + region_size();
}

inline size_t ParMarkBitMap::region_size() const {
  return _region_size;
}

inline size_t ParMarkBitMap::size() const {
  return _beg_bits.size();
}

inline bool ParMarkBitMap::is_marked(idx_t bit) const {
  return _beg_bits.at(bit);
}

inline bool ParMarkBitMap::is_marked(HeapWord* addr) const {
  return is_marked(addr_to_bit(addr));
}

inline bool ParMarkBitMap::is_marked(oop obj) const {
  return is_marked(cast_from_oop<HeapWord*>(obj));
}

inline bool ParMarkBitMap::is_unmarked(idx_t bit) const {
  return !is_marked(bit);
}

inline bool ParMarkBitMap::is_unmarked(HeapWord* addr) const {
  return !is_marked(addr);
}

inline bool ParMarkBitMap::is_unmarked(oop obj) const {
  return !is_marked(obj);
}

inline size_t ParMarkBitMap::bits_to_words(idx_t bits) {
  return bits << obj_granularity_shift();
}

inline ParMarkBitMap::idx_t ParMarkBitMap::words_to_bits(size_t words) {
  return words >> obj_granularity_shift();
}

inline bool ParMarkBitMap::mark_obj(HeapWord* addr) {
  return _beg_bits.par_set_bit(addr_to_bit(addr));
}

inline bool ParMarkBitMap::mark_obj(oop obj) {
  return mark_obj(cast_from_oop<HeapWord*>(obj));
}

inline ParMarkBitMap::idx_t ParMarkBitMap::addr_to_bit(HeapWord* addr) const {
  DEBUG_ONLY(verify_addr(addr);)
  return words_to_bits(pointer_delta(addr, region_start()));
}

inline HeapWord* ParMarkBitMap::bit_to_addr(idx_t bit) const {
  DEBUG_ONLY(verify_bit(bit);)
  return region_start() + bits_to_words(bit);
}

inline ParMarkBitMap::idx_t ParMarkBitMap::align_range_end(idx_t range_end) const {
  // size is aligned, so if range_end <= size then so is aligned result.
  assert(range_end <= size(), "range end out of range");
  return align_up(range_end, BitsPerWord);
}

inline ParMarkBitMap::idx_t ParMarkBitMap::find_obj_beg(idx_t beg, idx_t end) const {
  return _beg_bits.find_first_set_bit_aligned_right(beg, end);
}

inline HeapWord* ParMarkBitMap::find_obj_beg(HeapWord* beg, HeapWord* end) const {
  const idx_t beg_bit = addr_to_bit(beg);
  const idx_t end_bit = addr_to_bit(end);
  const idx_t search_end = align_range_end(end_bit);
  const idx_t res_bit = MIN2(find_obj_beg(beg_bit, search_end), end_bit);
  return bit_to_addr(res_bit);
}

inline HeapWord* ParMarkBitMap::find_obj_beg_reverse(HeapWord* beg, HeapWord* end) const {
  const idx_t beg_bit = addr_to_bit(beg);
  const idx_t end_bit = addr_to_bit(end);
  const idx_t res_bit = _beg_bits.find_last_set_bit_aligned_left(beg_bit, end_bit);
  return bit_to_addr(res_bit);
}

#ifdef  ASSERT
inline void ParMarkBitMap::verify_bit(idx_t bit) const {
  // Allow one past the last valid bit; useful for loop bounds.
  assert(bit <= _beg_bits.size(), "bit out of range");
}

inline void ParMarkBitMap::verify_addr(HeapWord* addr) const {
  // Allow one past the last valid address; useful for loop bounds.
  assert(addr >= region_start(),
         "addr too small, addr: " PTR_FORMAT " region start: " PTR_FORMAT, p2i(addr), p2i(region_start()));
  assert(addr <= region_end(),
         "addr too big, addr: " PTR_FORMAT " region end: " PTR_FORMAT, p2i(addr), p2i(region_end()));
}
#endif  // #ifdef ASSERT

#endif // SHARE_GC_PARALLEL_PARMARKBITMAP_INLINE_HPP
