import { Component, EventEmitter, Input, Output } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

interface FilterOption {
  value: string;
  label: string;
}

@Component({
  selector: 'app-filter-bar',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './filter-bar.component.html'
})
export default class FilterBarComponent {
  @Input() filterOptions: FilterOption[] = [];
  @Input() currentFilter: string = 'all';
  @Input() searchPlaceholder: string = 'Buscar...';
  @Input() color: 'blue' | 'pink' | 'green' | 'indigo' = 'blue';
  
  @Output() filterChange = new EventEmitter<string>();
  @Output() searchChange = new EventEmitter<string>();
  
  get colorClasses(): string {
    switch (this.color) {
      case 'pink':
        return 'bg-pink-50 border-pink-100 text-pink-800 hover:bg-pink-100';
      case 'green':
        return 'bg-green-50 border-green-100 text-green-800 hover:bg-green-100';
      case 'indigo':
        return 'bg-indigo-50 border-indigo-100 text-indigo-800 hover:bg-indigo-100';
      default: // blue
        return 'bg-blue-50 border-blue-100 text-blue-800 hover:bg-blue-100';
    }
  }
  
  get activeButtonClasses(): string {
    switch (this.color) {
      case 'pink':
        return 'bg-pink-100';
      case 'green':
        return 'bg-green-100';
      case 'indigo':
        return 'bg-indigo-100';
      default: // blue
        return 'bg-blue-100';
    }
  }
  
  onFilterClick(value: string): void {
    this.filterChange.emit(value);
  }
  
  onSearch(event: any): void {
    this.searchChange.emit(event.target.value);
  }
}
