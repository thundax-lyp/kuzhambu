import type { MouseEvent } from "react";
import { Button } from "@/components/ui/button";
import {
    Pagination,
    PaginationContent,
    PaginationItem,
    PaginationNext,
    PaginationPrevious
} from "@/components/ui/pagination";
import {
    Select,
    SelectContent,
    SelectGroup,
    SelectItem,
    SelectTrigger,
    SelectValue
} from "@/components/ui/select";
import {
    formatCount,
    hasListValue,
    type SearchFormState
} from "@/pages/discovery/search/search-utils";

const PAGE_SIZE_OPTIONS = ["10", "20", "50"] as const;

interface MultiOptionControlProps {
    description: string;
    label: string;
    name: keyof SearchFormState;
    onToggle: (name: keyof SearchFormState, value: string) => void;
    options: ReadonlyArray<{
        label: string;
        value: string;
    }>;
    value: string;
}

export const MultiOptionControl = ({
    description,
    label,
    name,
    onToggle,
    options,
    value
}: MultiOptionControlProps) => {
    return (
        <div className="portal-filter-field" role="group" aria-label={label}>
            <span>{label}</span>
            <input name={name} type="hidden" value={value} />
            <div className="portal-discovery-samples">
                {options.map((option) => {
                    const selected = hasListValue(value, option.value);

                    return (
                        <Button
                            aria-pressed={selected}
                            key={option.value}
                            type="button"
                            variant={selected ? "default" : "outline"}
                            onClick={() => onToggle(name, option.value)}
                        >
                            {option.label}
                        </Button>
                    );
                })}
            </div>
            <em>{description}</em>
        </div>
    );
};

interface DiscoveryPaginationProps {
    currentPage: number;
    disabled: boolean;
    pageSize: number;
    total: number;
    onChange: (pageNo: number, pageSize: number) => void;
}

export const DiscoveryPagination = ({
    currentPage,
    disabled,
    pageSize,
    total,
    onChange
}: DiscoveryPaginationProps) => {
    const totalPage = Math.max(1, Math.ceil(total / pageSize));
    const canGoPrevious = currentPage > 1;
    const canGoNext = currentPage < totalPage;
    const handlePageClick =
        (enabled: boolean, nextPage: number) => (event: MouseEvent<HTMLAnchorElement>) => {
            event.preventDefault();
            if (disabled || !enabled) {
                return;
            }
            onChange(nextPage, pageSize);
        };

    return (
        <div className="portal-discovery-pagination">
            <Pagination aria-label="搜索结果分页" className="portal-discovery-pager">
                <PaginationContent>
                    <PaginationItem>
                        <PaginationPrevious
                            aria-disabled={disabled || !canGoPrevious}
                            aria-label="上一页"
                            data-disabled={disabled || !canGoPrevious}
                            href="#"
                            text=""
                            onClick={handlePageClick(canGoPrevious, Math.max(1, currentPage - 1))}
                        />
                    </PaginationItem>
                    <PaginationItem>
                        <span className="portal-discovery-page-indicator">
                            第 {currentPage} / {totalPage} 页
                        </span>
                    </PaginationItem>
                    <PaginationItem>
                        <PaginationNext
                            aria-disabled={disabled || !canGoNext}
                            aria-label="下一页"
                            data-disabled={disabled || !canGoNext}
                            href="#"
                            text=""
                            onClick={handlePageClick(canGoNext, currentPage + 1)}
                        />
                    </PaginationItem>
                </PaginationContent>
            </Pagination>
            <div className="portal-discovery-pagination-extra">
                <span>共 {formatCount(total)} 条</span>
                <Select
                    value={String(pageSize)}
                    onValueChange={(value) => onChange(1, Number.parseInt(value, 10) || pageSize)}
                >
                    <SelectTrigger aria-label="每页数量" size="sm">
                        <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                        <SelectGroup>
                            {PAGE_SIZE_OPTIONS.map((option) => (
                                <SelectItem key={option} value={option}>
                                    每页 {option} 条
                                </SelectItem>
                            ))}
                        </SelectGroup>
                    </SelectContent>
                </Select>
            </div>
        </div>
    );
};
