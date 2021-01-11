"""
Essentially, we're reproducing the code from the R package AlgDesign:
```r
library(AlgDesign)

des<-gen.factorial(3,16)
desBlk<-optBlock(~, des, rep(8,4), nRepeats=20)

desBlk$Blocks$B1
```
However, this code does not take care of all rules (I think), e.g., that we do not
want strongly dominant alternatives or that attribute levels should occur
roughly the same time within each block.
"""
from tqdm import tqdm
import numpy as np

from test_matrices import *


np.random.seed(41)  # Change random seed to generate a new experiment.

n_optimization_steps = 100  # Number of iterations that optimize the matrix.
n_optimization_restarts = 5  # Number of times a choice experiment is generated (starting
                              # from a random state).

n_b = 4  # Number of blocks.
n_S = 8  # Number of choice sets per block.
n_a = 5  # Number of attributes we want to check.
n_m = 3  # Number of transport modes we want to check.

num_attributes_tot = 1 + n_a * n_m
attribute_permutations = np.power(3, num_attributes_tot)
# Instead of building the whole permutation matrix, we simply provide a function
# that returns the corrsponding row of the matrix.
# permutation_matrix = np.empty((attribute_permutations, num_attributes_tot))
def permutation_matrix_at(row):
    return np.array([[int(c) - 1 for c in np.base_repr(row, base=3).zfill(num_attributes_tot)]])

# Check that our function represents the whole combinatorial space.
assert np.array_equal(permutation_matrix_at(0), np.array([[-1] * num_attributes_tot]))
assert np.array_equal(permutation_matrix_at(attribute_permutations - 1), np.array([[1] * num_attributes_tot]))

# Function to compute strongly dominant modes.
def strongly_dominant(row):
    highest_attr = -1
    for attr in range(n_a):
        vals = np.array([row[attr + mode * n_a] for mode in range(n_m)])
        new_highest_attr = np.argmax(vals)
        if highest_attr != -1 and highest_attr != new_highest_attr:
            return True
        if np.sum(vals == np.max(vals)) > 1:
            return True
        highest_attr = new_highest_attr
    return False
    
# Function to compute weakly dominant modes.
def weakly_dominant(row):
    # For each mode, we look if its attributes are all greater or equal than
    # all the attributes of any other mode.
    vals = np.array([row[mode * n_a:(mode + 1) * n_a] for mode in range(n_m)])
    for mode in range(n_m):
        mode_weakly_dominant = True
        for other_mode in range(n_m):
            if mode != other_mode:
                mode_weakly_dominant = mode_weakly_dominant and np.greater_equal(vals[mode], vals[other_mode]).all()
        if mode_weakly_dominant:
            return True
    return False

# Computes the differences in attribute values for each row.
def row_attr_diff(row):
    vals = np.array([row[mode * n_a:(mode + 1) * n_a].sum() for mode in range(n_m)])
    diff = np.max(vals) - np.min(vals)
    return diff
    
# A function that checks if all the rules are adhered to by the design matrix.
# This variable denotes the attribute groups in the design matrix.
alternative_attributes = [list(range(i * n_a + 0, (i + 1) * n_a)) for i in range(n_m)]
def check_rules(dm):
    # Rule of no strongly dominant alternatives.
    dominant_alternatives = np.apply_along_axis(strongly_dominant, axis=1, arr=dm).all()

    # Rule of a maximum of 4 weakly dominant alternatives per block.
    weakly_dominant_alternatives = np.apply_along_axis(weakly_dominant, axis=1, arr=dm)
    max_4_weakly_dominant = True
    for block in range(n_b):
        num_weak_dominant_alternatives = weakly_dominant_alternatives[(block * n_S):((block + 1) * n_S)].sum()
        max_4_weakly_dominant = max_4_weakly_dominant and (num_weak_dominant_alternatives <= 4)

    return dominant_alternatives and max_4_weakly_dominant

assert check_rules(fail_dominant_rule_1) == False
assert check_rules(fail_dominant_rule_2) == False
assert check_rules(pass_dominant_rule_1) == True
assert check_rules(pass_dominant_rule_2) == True
assert check_rules(fail_weakly_dominant_rule_1) == False
assert check_rules(fail_weakly_dominant_rule_2) == False
assert check_rules(pass_weakly_dominant_rule_1) == True
assert check_rules(pass_weakly_dominant_rule_2) == True

# Create a random initial design matrix.
def create_design_matrix():
    design_matrix = np.empty((0, num_attributes_tot))
    for _ in range(n_b * n_S):
        redraw_matrix = True
        while redraw_matrix:
            new_design_matrix = np.append(design_matrix, permutation_matrix_at(
                np.random.randint(0, attribute_permutations - 1)), axis=0)
            redraw_matrix = not check_rules(new_design_matrix)
        design_matrix = new_design_matrix
    return design_matrix

# The optimization function, takes the design matrix as input.
def optimization_fn(dm, criterion='Dpc'):
    # The idea here is to normalize by subtracting the block mean from each row.
    dm_norm = dm.copy()

    # Dpc-criterion to optimize within-block choice sets.
    Dis = []
    for i in range(n_b):
        # Do block normalization.
        dmi = dm[(i * n_S):((i + 1) * n_S), :]
        block_mean = np.mean(dmi)
        dmi = dmi - block_mean
        dm_norm[(i * n_S):((i + 1) * n_S), :] = dmi

        # Compute the determinant of this block.
        Mi = np.matmul(np.transpose(dmi), dmi) / n_S
        Di = np.float_power(np.linalg.det(Mi), 1 / (n_a * n_m + 1))
        Dis.append(Di)
    # Compute the final Dpc criterion.
    D_Dpc = np.float_power(np.prod(Dis), 1 / n_b)

    # Main D-criterion optimization. We could also use dm instead of dm_norm, but it seems
    # dm_norm is more appropriate (see http://finzi.psych.upenn.edu/library/AlgDesign/html/blockOpt.html).
    M = np.matmul(np.transpose(dm_norm), dm_norm) / (n_b * n_S)
    D = np.float_power(np.linalg.det(M), 1 / (n_a * n_m + 1))

    if criterion == 'Dpc':
        D_final = D_Dpc
    else:
        D_final = D

    # Balance weakly dominant alternatives between blocks.
    weakly_dominant_alternatives = np.apply_along_axis(weakly_dominant, axis=1, arr=dm)
    blocked_weakly_dominant_alternatives = [weakly_dominant_alternatives[(i * n_S):((i + 1) * n_S)].sum() for block in range(n_b)]
    biggest_diff = np.max(blocked_weakly_dominant_alternatives) - np.min(blocked_weakly_dominant_alternatives)
    weakly_dominant_bonus = (4 - biggest_diff) / 4

    # Attribute levels should occur roughly the same time within each block.
    counts = []
    for i in range(n_b):
        mones = np.count_nonzero(dm[(i * n_S):((i + 1) * n_S), :] == -1)
        zeros = np.count_nonzero(dm[(i * n_S):((i + 1) * n_S), :] == 0)
        ones = np.count_nonzero(dm[(i * n_S):((i + 1) * n_S), :] == 1)
        counts.append([mones, zeros, ones])
    counts = np.array(counts)
    mones_diff = np.max(counts[:, 0]) - np.min(counts[:, 0])
    zeros_diff = np.max(counts[:, 1]) - np.min(counts[:, 1])
    ones_diff = np.max(counts[:, 2]) - np.min(counts[:, 2])
    attribute_levels_bonus = ((n_a * n_b * n_S) - mones_diff) + ((n_a * n_b * n_S) - zeros_diff) + \
        ((n_a * n_b * n_S) - ones_diff)

    # Minimizing the difference between sum of attribute levels (between alternatives).
    attr_diffs = np.apply_along_axis(row_attr_diff, axis=1, arr=dm)
    attr_diff_bonus = (n_a * n_b * n_S) - attr_diffs.sum()

    # The last column must be equally distributed.
    mones = np.count_nonzero(dm[:, -1] == -1)
    zeros = np.count_nonzero(dm[:, -1] == 0)
    ones = np.count_nonzero(dm[:, -1] == 1)
    last_col_diff = np.max([mones, zeros, ones]) - np.min([mones, zeros, ones])
    purpose_bonus = (n_b * n_S) - last_col_diff

    # Return a weighted optimization function that combines the different parts.
    return D_final + 0.1 * weakly_dominant_bonus + 0.1 * attribute_levels_bonus + 0.1 * attr_diff_bonus + purpose_bonus

if __name__ == "__main__":
    orig_matrices = []
    optim_matrices = []
    criteria = []

    for optimization in range(n_optimization_restarts):
        design_matrix = create_design_matrix()
        orig_matrices.append(design_matrix)
    
        # Algorithm that optimizes the design_matrix by a) choosing a random row to replace,
        # b) drawing a random new row, c) checking if the new design matrix corresponds to
        # all rules, d) computes the optimization function and e) replaces the row with the
        # new one in case the new design matrix is better.
        criterion = 0
        for i in tqdm(range(n_optimization_steps)):
            redraw_matrix = True
            while redraw_matrix:
                new_design_matrix = design_matrix.copy()
                new_row = permutation_matrix_at(np.random.randint(0, attribute_permutations - 1))
                new_design_matrix[np.random.randint(0, new_design_matrix.shape[0]), :] = new_row[0, :]
                new_criterion = optimization_fn(new_design_matrix, criterion='Dcp')

                checks_passed = check_rules(new_design_matrix)
                optimization_passed = new_criterion > criterion
                new_design_matrix_good = checks_passed and optimization_passed

                redraw_matrix = not new_design_matrix_good
            criterion = new_criterion
            design_matrix = new_design_matrix

        optim_matrices.append(design_matrix)
        criteria.append(criterion)

    print(criteria, f'(best one is {np.argmax(criteria)}).')
    print(optim_matrices[np.argmax(criteria)])
